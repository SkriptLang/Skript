#!/bin/bash
find src/main/java -type f -exec sed -i "s/INSERT VERSION/$1/g" {} \;
