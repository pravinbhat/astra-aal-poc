import re
from collections import defaultdict
import time
import datetime

log_file_path = 'log.txt'  # replace with the actual path to your log file

log_data = defaultdict(dict) # {request_id:{event_type:timestamp, duration:duration}}

# Regular expression pattern to extract relevant information
pattern = re.compile(r'(\d+:\d+:\d+\.\d+)(.*)com\.datastax\.oss\.driver\.internal\.core(.*)\[bhatman\|(\d+)[|\]](.*)')
duration_pattern = re.compile(r'(.*)\((\d+) ms\)(.*)')
duration_pattern_second = re.compile(r'(.*)\((\d+) s\)(.*)')
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
                if "SELECT * FROM airport WHERE airport=? LIMIT 1 [airport='NY-London']" not in remained:
                    # not interested, ignore
                    del log_data[request_id]
                    continue
                duration_match = duration_pattern.match(remained)
                if not duration_match:
                    duration_match = duration_pattern_second.match(remained)
                    if not duration_match:
                        print(f"Error parsing duration: {remained}")
                        log_data[request_id]["duration"] = 'NA'
                    else:
                        _, duration, _ = duration_match.groups()
                        log_data[request_id]["duration"] = duration * 1000
                else:
                    _, duration, _ = duration_match.groups()
                    log_data[request_id]["duration"] = duration
            else:
                # Ignore everything else
                continue

            log_data[request_id][event_type] = timestamp

def str_to_milliseconds(timestamp):
    dt = datetime.datetime.strptime(timestamp, '%H:%M:%S.%f')
    return (dt.second + dt.microsecond/1000000)*1000

# Print the result
print("requests_id,start,sent,got,duration,queue")
avg_time_queued = 0
for request_id in log_data.keys():
    time_queued = str_to_milliseconds(log_data[request_id]['sent']) - str_to_milliseconds(log_data[request_id]['start'])
    avg_time_queued += time_queued
    if time_queued > 3:
        print(f"{request_id},{log_data[request_id]['start']},{log_data[request_id]['sent']},{log_data[request_id]['got']},{log_data[request_id]['duration']},{time_queued}")

avg_time_queued /= len(log_data)
print(f"Average time queued: {avg_time_queued} ms")