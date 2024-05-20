set -e # Will stop as soon as there are any assertion failures.
for f in ./etc/postman/*.json
  do newman run $f -r cli,progress --bail failure
done