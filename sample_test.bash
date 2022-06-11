#!/bin/bash

curl -d '{"url":"https://abc"}' -H "Content-Type: application/json" -X POST localhost:8080/report

curl -d '{"url":"https://foo"}' -H "Content-Type: application/json" -X POST localhost:8080/report

for VARIABLE in {1..9}
do
    curl -d '{"url":"https://abc"}' -H "Content-Type: application/json" -X POST localhost:8080/report

done

curl -d '{"url":"https://foo"}' -H "Content-Type: application/json" -X POST localhost:8080/report

for VARIABLE in {1..2}
do
    curl -d '{"url":"https://abc"}' -H "Content-Type: application/json" -X POST localhost:8080/report

done

for VARIABLE in {1..2}
do
    curl -d '{"url":"https://foo"}' -H "Content-Type: application/json" -X POST localhost:8080/report
done
