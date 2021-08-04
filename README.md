# rss-to-email

RSS subscription service built using Datomic Ions. Hosted at https://rssto.email.

# Use as a template

This repo can be used as a batteries-included version of the ion starter project, because some of the following things are already set up:

- lambda proxies
- CORS headers
- logging
- tests
- DB redirection for tests

# Set up

## Requirements
1. Clojure
2. A Datomic Cloud deployment. (You can set one up easily using a cloudformation template, following [these instructions](https://docs.datomic.com/cloud/getting-started/getting-started.html))
3. [Babashka](https://github.com/babashka/babashka) (if you want to run the deploy script)
4. SES set up for your Amazon account

## Configuration
- Edit `config.edn` with the correct :region, :system and :endpoint from your Datomic Cloud deployment.
- Edit the constants in `datomic.ion.rsstoemail.mailer` for the correct mail from address and redirect URLs.
- For the poller to actually run, find the `*-poll-all` Lambda in your AWS console, and add an [EventBridge rule so it runs on a schedule](https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-create-rule-schedule.html)

## Deploying

One of:
- If you have Babashka, run `./deploy.clj`.
- [Push and deploy](https://docs.datomic.com/cloud/ions/ions-tutorial.html#invoke-push) using `ion-dev` commands directly.

## Testing

- `clojure -Mtest` to run unit and integration tests
- To verify that everything is set up correctly, you can run some commands from the repl:
```clojure
;; load namespaces and ensure DB schema has been created
(load-file "siderail/setup.repl")
;; request a subscription
(rsstoemail/request-sub (rsstoemail/get-connection) "<email>" "<feed>")
;; run poller
(lambdas/poll-all {})
```

