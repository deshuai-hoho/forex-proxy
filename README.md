# A local proxy for Forex rates 

Expected complete time: 29th Dec
  * Some additional changes in 1st Jan

## Functional Requirements
1. Returns an exchange rate when provided with 2 supported currencies by calling One-Frame;
2. The rate should not be older than 5 minutes
3. The service should support at least 10,000 successful requests per day with 1 API token
    * While One-Frame service supports a maximum of 1000 requests per day for any given authentication token.


## Plan

To solve the conflict that our proxy service should have 10 times throughout than OneFrame, the intuitive method is trying to cache the query result in our service.

### Assumptions
  * We have 9 currencies, the number of pairs will be 9 * (9-1) = 72;
  * Cache Expiry Time: 5 minutes (as per your requirement).
  * Daily Quota for OneFrame API: 1,000 requests per token.
  * Required Daily Quota for Your Service: 10,000 requests per token.
  * The services serves 100~1000 users

### Scenario Analysis
  * for one user(one token)
    * if only query one pair, each time we cache the result for 5 min, then we only need to query OneFrame 24*60/5 = 288 times a day, the user can query as much as he want.
    * if the user query 72 pairs together and for every 5 min, then each time his query will miss cache, which leads to 1000/72*5 = 69 min to use up the token's quota.
  * Multiple users(multiple token)
    * if we set a glabal forex pair rates cache, with more other users, certain user's requests are more likely to hit the cache. 
    * if we have at least Math.ceil(288 * 72 / 1000) = 21 tokens we can achive a status that update all 72 pairs' rates every 5 min and therefore support target request quota.
    * no less than 21-tokens is a easy and reasonable target for a service.
    
### Solution
  With the analysis above, for extreame cases we cannot support the target request quota. So instead of using each user's token to update the forex rates cache, it is more reasonable for this service to manage a token pool and use each token more averagely. Here we made another assumption.

  * <b>Once users got their token, any amount usage under the quota of OneFrame will not affect their charge status.</b>

  <b>Process</b>

  The whole getRates process will be:
  1. User's Request comes in forex-proxy, record their quota. Reject it if excceed 10000.
  2. The request enter `OneFrameLive`
     1. New token will be add to `tokenPool`
     2. Check if the request pair rates is in the cache, if hit the cache, return the result.
     3. Get a token from the `TokenPool` by selecting the most quota left one, and request OneFrame
     4. Record that token's quota.
     5. Update the cache
     6. return the result.

  There are still extreame cases we may have can will cause the user reach their OneFrame quota way earlier than expected.
  The Cold Start case will be:
  1. After the service start running, the `tokenPool` is empty or just several tokens.
  2. The user starts to query many pair of rates every 5 min, very soon his token quota in OneFrame is used up.

* Fallback Solution

    If we just return `Excceed Quata` error to the user, it will make no sense because the forex-proxy still has the capability to serve the query once there are more token in tokenPool. And the users will have no clear expectation when they can request again.

    Here I would like build a throttle strategy to providing a better user experience. When the token's quota in OneFrame reach a threshold, the forex-proxy will freeze its request for a certain time period.
    The throttle strategy can be very dynamic by some factors like:
        * time point of the day
        * usage portion of the quota.
        * size of token pool
    
    We can start with a simple one and optimize it by real-world data in a progressive way.

    <b>SimpleThrottleStrategy</b>

        * After query OneFrame with `tokenA`, update its quota usage.
        * for `tokenA`, if its quota usage exceed:
        * 50%(500) and the size of tokenPool is smaller than:
            * 5, freeze 30 min
        * 60%(670) and the size of tokenPool is smaller than:
            * 10, freeze 45 min
            * 5, freeze 1 hour
        * 95% and the size of tokenPool is smaller than:
            * 10 freeze 1.5 hour
            * 5 freeze  2 hour
* Open Discussion:

    For a proxy service, it is reasonable to assume that
    * we could have stored user's tokens
    * we have manage dozens of in-house tokens. 
    
    If this is the case then we coulld have initialize the token pool so that the service can handle the cold start scenario we described above.
 

  

  
        