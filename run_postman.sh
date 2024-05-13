for f in ./etc/postman/*.json
  do newman run $f -r cli,progress --bail --reporter-cli-silent
done