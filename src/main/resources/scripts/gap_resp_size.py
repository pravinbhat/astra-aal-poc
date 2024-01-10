import asyncio
import aiohttp
import time
import json

# Endpoint URL
post_url = "http://localhost:8080/flights/airports"

# Large response body 
post_body = "airport-1,airport-2,airport-3,airport-4,airport-5,airport-6,airport-7,airport-8,airport-9,airport-10"

# Small response body
#post_body = "airport-101,airport-102,airport-103,airport-104,airport-105,airport-106,airport-107,airport-108,airport-109,airport-110"

# Timing control
calls = 25
total_duration = 3 # Seconds

async def fetchPost(session, url, body):
    # First GET request
    async with session.post(url, data=body) as response:
        # Process the response here if needed
        response_data = await response.text()
        parsed_json = json.loads(response_data)
        
async def main():
    async with aiohttp.ClientSession() as session:
        end_time = time.time() + total_duration
        total_calls = 0
        while time.time() < end_time:
            start = time.time()

            # Create a list of tasks for the GET requests
            tasks = [fetchPost(session, post_url, post_body) for _ in range(calls)]
            # tasks = [fetchSync(session, url_1, url_2) for _ in range(async_bulk_calls)]
            # Run tasks concurrently without waiting for the response
            await asyncio.gather(*tasks)
            end = time.time()

            # Calculate and wait for the remaining time in the 1-second window, if any
            time_taken = (end - start)
            # time_to_wait = 1 - (end - start)
            # if time_to_wait > 0:
            #     await asyncio.sleep(time_to_wait)
            total_calls += calls
            print(f"Start time: {start} End time: {end} Time taken for {calls} calls: {time_taken}. Total calls: {total_calls}")
            # time.sleep(1)
    
        print(f"Total calls made in {total_duration} seconds: {total_calls}")

# Run the async main function
asyncio.run(main())
