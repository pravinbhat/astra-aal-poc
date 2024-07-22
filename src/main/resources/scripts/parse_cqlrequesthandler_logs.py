import re
from collections import defaultdict
import datetime
import sys

# take file path as an input
if len(sys.argv) < 2:
    log_file_path = input("Enter the path to the log file: ")
else:
    log_file_path = sys.argv[1]
    
if len(sys.argv) > 2:
    queue_time_over_millis = int(sys.argv[2])
else:
    queue_time_over_millis = 300 # Change this based in your threshold
log_data = defaultdict(dict) # {request_id:{event_type:timestamp, duration:duration}}
query_data = defaultdict(dict) # {query:{calls:count, queued_calls:count, duration:duration_in_ms}}

# Regular expression pattern to extract relevant information
pattern = re.compile(r'\[TRACE\] \d+-\d+-\d+\s+(\d+:\d+:\d+\,\d+)\s*com\.datastax\.oss\.driver\.internal\.core\.cql\.CqlRequestHandler.*?\[.*?\|(\d+).*?](.*)')

with open(log_file_path, 'r') as file:
    for line in file:
        match = pattern.match(line)
        if match:
            #timestamp, _, logger, request_id, remained = match.groups()
            timestamp, request_id, remained = match.groups()

            if "Creating new handler for request" in remained:
                event_type = "start"
            elif "Request sent on" in remained:
                event_type = "sent"
            elif "Got result" in remained:
                event_type = "got"
            else:
                # Ignore everything else
                continue

            log_data[request_id][event_type] = timestamp
            #print("Added", request_id, event_type, timestamp)

def str_to_milliseconds(timestamp):
    dt = datetime.datetime.strptime(timestamp, '%H:%M:%S,%f')
    return (dt.second + dt.microsecond/1000000)*1000

print("Total logged requests: ", len(log_data))

# Print the result
print("============================================ Queued Requests ==========================================")
print("Request Id, Start time, Sent time, Response received time, Queue (ms)")
print("=======================================================================================================")
avg_time_queued = 0
queued_count = 0
for request_id in log_data.keys():
    #print("processing request", request_id) 
    #print("processing request", request_id, log_data[request_id]) 
    if 'sent' in log_data[request_id] and 'start' in log_data[request_id] and 'got' in log_data[request_id]:
        time_queued = str_to_milliseconds(log_data[request_id]['sent']) - str_to_milliseconds(log_data[request_id]['start'])
        if time_queued > queue_time_over_millis:
            queued_count += 1
            print(f"{request_id}, {log_data[request_id]['start']}, {log_data[request_id]['sent']}, {log_data[request_id]['got']}, {time_queued}")
            avg_time_queued += time_queued

print("=======================================================================================================")

print("\n=============================================== Results ===============================================")
print(f"Total requests, Queued requests(over {queue_time_over_millis} ms), Average time queued")
print("=======================================================================================================")
if avg_time_queued > 0:
    avg_time_queued /= queued_count
print(f"{len(log_data)}, {queued_count}, {avg_time_queued:.2f} ms")
print("=======================================================================================================")
