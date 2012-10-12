# /bin/bash
ls log/competitor*.txt | xargs -n1 tail -n1 | awk '{ print $1 " - Competitor #" NR }' | sort
