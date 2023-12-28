# A local proxy for Forex rates [WIP]

Expected complete time: 29th Dec

## Functional Requirements
1. Returns an exchange rate when provided with 2 supported currencies by calling One-Frame;
2. The rate should not be older than 5 minutes
3. The service should support at least 10,000 successful requests per day with 1 API token
    * While One-Frame service supports a maximum of 1000 requests per day for any given authentication token.


## Plan

### Solution for functional Requirements
To solve the conflict that our proxy service should have 10 times throughout than OneFrame, the intuitive method is trying to cache the query result in our service.

* Assumptions
  * We have 9 currencies, the number of pairs will be 9 * (9-1) = 72;
  * Cache Expiry Time: 5 minutes (as per your requirement).
  * Daily Quota for OneFrame API: 1,000 requests per token.
  * Required Daily Quota for Your Service: 10,000 requests per token.

* Scenario Analysis
  * for one user(one token)
    * if only query one pair, each time we cache the result for 5 min, then we only need to query OneFrame 24*60/5 = 288 times a day, the user can query as much as he want.
    * if the user query 72 pairs together and for every 5 min, then each time his query will miss cache, which leads to 1000/72*5 = 69 min to use up the token's quota.
  * Optimazation
    * we want the OneFrame queries to be allocated averagely by each token, which means we don't just update cache only when expired, but update it as long as it is updated by other tokens.
  * Open solution
    * If we can manage a token pool, and have at least 288 * 72 / 1000 = 21 tokens we can achive update all 72 pairs' rates every 5 min and therefore support target request quota.
    * With that being said, our service need to implement a separate token usage system independent from the OneFrame's.