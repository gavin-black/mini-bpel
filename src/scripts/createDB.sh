#!/bin/bash

mysql -b -u foo -pfoo -e 'source scripts/createDB.sql'
