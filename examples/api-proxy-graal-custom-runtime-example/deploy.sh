docker build . -t example-micronaut-runtime
mkdir build
docker run --rm --entrypoint cat example-micronaut-runtime  /home/application/function.zip > build/function.zip
aws lambda create-function --function-name micronaut-runtime \
--zip-file fileb://build/function.zip --handler function.handler --runtime provided \
--role arn:aws:iam::881337894647:role/lambda_basic_execution