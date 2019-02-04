mkdir -p build
aws lambda invoke --function-name micronaut-runtime --payload '{"resource": "/{proxy+}", "path": "/ping", "httpMethod": "GET"}' build/response.txt
echo "RESPONSE:"
echo "---------"
cat build/response.txt