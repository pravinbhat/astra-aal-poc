import re
from collections import defaultdict
import datetime
import sys

# take file path as an input
if len(sys.argv) < 2:
    log_file_path = input("Enter the path to the log file: ")
else:
    log_file_path = sys.argv[1]

queue_time_over_millis = 3 # Change this based in your threshold
log_data = defaultdict(dict) # {request_id:{event_type:timestamp, duration:duration}}
query_data = defaultdict(dict) # {query:{calls:count, queued_calls:count, duration:duration_in_ms}}

# Regular expression pattern to extract relevant information
pattern = re.compile(r'(\d+:\d+:\d+\.\d+)\s*(.*?)\s*com\.datastax\.oss\.driver\.internal\.core.(.*?)\s*:\s*\[.*?\|(\d+).*?](.*)')
duration_pattern =        re.compile(r'\[(.*?)\].*?\((.*?) ms\).*?(select .*?)\[', re.IGNORECASE)
duration_pattern_second = re.compile(r'\[(.*?)\].*?\((.*?) s\).*?(select .*?)\[', re.IGNORECASE)

with open(log_file_path, 'r') as file:
    for line in file:
        match = pattern.match(line)
        if match:
            timestamp, _, logger, request_id, remained = match.groups()

            if "Creating new handler for request" in remained:
                event_type = "start"
            elif "Request sent on" in remained:
                event_type = "sent"
            elif "Got result" in remained:
                event_type = "got"
            elif "RequestLogger" in logger:
                if "SELECT " not in remained.upper():
                    if request_id in log_data:
                        del log_data[request_id]
                        print(f"Deleted {request_id} for RequestLogger with log: {remained}")
                    else:
                        print(f"Ignoring {request_id} for RequestLogger with log: {remained}")
                    continue
                duration_match = duration_pattern.match(remained)
                if not duration_match:
                    duration_match = duration_pattern_second.match(remained)
                    if not duration_match:
                        print(f"Error parsing duration: {remained}")
                        log_data[request_id]["duration"] = 'NA'
                    else:
                        _, duration, query = duration_match.groups()
                        log_data[request_id]["duration"] = float(duration) * 1000
                        log_data[request_id]["query"] = query
                else:
                    _, duration, query = duration_match.groups()
                    log_data[request_id]["duration"] = duration
                    log_data[request_id]["query"] = query
            else:
                # Ignore everything else
                continue

            log_data[request_id][event_type] = timestamp

def str_to_milliseconds(timestamp):
    dt = datetime.datetime.strptime(timestamp, '%H:%M:%S.%f')
    return (dt.second + dt.microsecond/1000000)*1000

# Print the result
print("============================================ Queued Requests ==========================================")
print("Request Id, Start time, Sent time, Response received time, Duration (ms), Queue (ms), Query")
print("=======================================================================================================")
avg_time_queued = 0
queued_count = 0
for request_id in log_data.keys():
    if 'sent' in log_data[request_id] and 'start' in log_data[request_id] and 'got' in log_data[request_id] and 'duration' in log_data[request_id]:
        time_queued = str_to_milliseconds(log_data[request_id]['sent']) - str_to_milliseconds(log_data[request_id]['start'])
        if 'query' in log_data[request_id]:
            qry = log_data[request_id]['query']
        if time_queued > queue_time_over_millis:
            queued_count += 1
            print(f"{request_id}, {log_data[request_id]['start']}, {log_data[request_id]['sent']}, {log_data[request_id]['got']}, {log_data[request_id]['duration']}, {time_queued}, {log_data[request_id]['query']}")
            avg_time_queued += time_queued
            if qry not in query_data:
                query_data[qry]['calls'] = 1
                query_data[qry]['queued_calls'] = 1
                query_data[qry]['duration'] = time_queued
            else:
                query_data[qry]['calls'] += 1
                query_data[qry]['queued_calls'] += 1
                query_data[qry]['duration'] += time_queued
        else:
            if qry not in query_data:
                query_data[qry]['calls'] = 1
                query_data[qry]['queued_calls'] = 0
                query_data[qry]['duration'] = 0
            else:
                query_data[qry]['calls'] += 1
print("=======================================================================================================")

print("\n=============================================== Results ===============================================")
print(f"Total requests, Queued requests(over {queue_time_over_millis} ms), Average time queued")
print("=======================================================================================================")
if avg_time_queued > 0:
    avg_time_queued /= queued_count
print(f"{len(log_data)}, {queued_count}, {avg_time_queued} ms")
print("=======================================================================================================")

print("\n=========================================== Results by Query ==========================================")
print(f"Query, Total requests, Queued requests(over {queue_time_over_millis} ms), Average time queued")
print("=======================================================================================================")
for qry in query_data.keys():
    if query_data[qry]['queued_calls'] > 0:
        avg_time_queued = query_data[qry]['duration'] / query_data[qry]['queued_calls']
        print(f"{qry}, {query_data[qry]['calls']}, {query_data[qry]['queued_calls']}, {avg_time_queued} ms")
print("=======================================================================================================")

