omtut-angular
=============

The [AngularJS tutorial](http://docs.angularjs.org/tutorial) rewritten in Om.

## Why?

The more resources out there that exist to help teach a piece of technology, the more that will learn it and contribute to the ecosystem. I really want this ecosystem to grow.

## Structure

Each step of the original tutorial corresponds to a branch of the format `step-{x}`.

This one isn't quite as in-depth as my other tutorial [omtut-starter](), which goes through all of the Om concepts in quite a bit of detail. That's a recommended starting point. The point of this one is to illustrate an approach to building a slightly more complicated Om app with some compare and contrast to a more familiar way of doing things.

Each step has lots of comments that go over what changed from the last step, why the change was made, and other ramblings.

## Tests

I've made an attempt to make some tests work, and some of them even do!

I'll confess that I am not the most avid tester, so not all of the Angular tutorial tests are present. I've had a difficult time reproducing some of them, paritcularly the end-to-end tests, for which PhantomJS doesn't work.

I borrowed some previous work from [sgrove's]() app [Omchaya]() (without that I probably would not have gotten far -- thanks Sean!) and added a few of my own bits and pieces.

What's working is:
+ A few tests!
+ A `leiningen` alias (`lein auto-test`) that watches tests for changes and reruns them.
+ A simple system for testing arbitrary Om components with PhantomJS.

Hopefully someone with more background in CLJS testing can improve upon/find better ways to go about this.

## Running

`lein dev` will run `lein cljsbuild auto dev` and `lein ring server` in parallel. Navigate to port 3000 and you're set.

## What's left out

+ Step 11 - REST and Custom Services: This involves use of Angular's $resource service, which doesn't really fit in well to the CLJS way of doing things.
+ Step 12 - Applying Animations: This is more of a CSS thing than something related to Om. I don't plan demonstrating this.

