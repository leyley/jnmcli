#!/bin/sh
RUN_OPTIONS=""
while read line; do    
    RUN_OPTIONS=$RUN_OPTIONS" "$line    
done < properties
java $RUN_OPTIONS Nmcli