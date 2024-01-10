import asyncio
import aiohttp
import time
import json

# Endpoint URL
url_1 = "http://localhost:8080/flights/NYC/9c7cba10-826d-11ee-bc9d-03a32cbeffc5"

# Add the airportId from the first response to the second URL OR hardcode the airportId in the URL
url_2 = "http://localhost:8080/flights/NYC"

# Timing control
calls = 30
total_duration = 3 # Seconds

async def fetchSync(session, url1, url2):
    # First GET request
    async with session.get(url1) as response1:
        # Process the response here if needed
        response_data1 = await response1.text()
        parsed_json = json.loads(response_data1)
        airportId = parsed_json['flight']['flightPk']['airportId']
        # url2 += "/" + airportId

        # Optionally use response_data1 to modify the second request

    # # Second GET request, dependent on the first
    async with session.get(url2) as response2:
        # Process the response here if needed
        response_data2 = await response2.text()
        parsed_json = json.loads(response_data2)

async def fetch1(session, url):
    # First GET request
    async with session.get(url) as response1:
        # Process the response here if needed
        response_data1 = await response1.text()
        parsed_json = json.loads(response_data1)
        airportId = parsed_json['flight']['flightPk']['airportId']

async def fetch2(session, url):
    # # Second GET request, dependent on the first
    async with session.get(url) as response2:
        # Process the response here if needed
        response_data2 = await response2.text()
        parsed_json = json.loads(response_data2)

async def main():
    async with aiohttp.ClientSession() as session:
        end_time = time.time() + total_duration
        total_calls = 0
        tot_time_taken1 = 0
        tot_time_taken2 = 0
        while time.time() < end_time:
            start = time.time()

            # Create a list of tasks for the GET requests
            # tasks = [fetchSync(session, url_1, url_2) for _ in range(calls)]
            tasks = [fetch1(session, url_1) for _ in range(calls)]
            # tasks2 = [fetch2(session, url_2) for _ in range(calls)]
            # Run tasks concurrently without waiting for the response
            # Calculate and wait for the remaining time in the 1-second window, if any
            await asyncio.gather(*tasks)
            end = time.time()
            time_taken1 = (end - start)
            tot_time_taken1 += time_taken1

            # await asyncio.gather(*tasks2)
            # end = time.time()
            # time_taken2 = (end - start)
            # tot_time_taken2 += time_taken2

            # time_to_wait = 1 - (end - start)
            # if time_to_wait > 0:
            #     await asyncio.sleep(time_to_wait)
            total_calls += calls
            print(f"Start: {start} End: {end} Time taken-1 for {calls} calls: {time_taken1}. Total calls: {total_calls}, AVG time/call {time_taken1/calls}")
            # print(f"Start: {start} End: {end} Time taken-2 for {calls} calls: {time_taken2}. Total calls: {total_calls}, AVG time/call {time_taken2/calls}")
            # time.sleep(0.2)
    
        print(f"Total calls made in {total_duration} seconds: {total_calls}, AVG time/call-1 {tot_time_taken1/total_calls}")
        print(f"Total calls made in {total_duration} seconds: {total_calls}, AVG time/call-2 {tot_time_taken2/total_calls}")

# Run the async main function
asyncio.run(main())
