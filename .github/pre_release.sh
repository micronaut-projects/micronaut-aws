#!/bin/bash
# $1 == GH_TOKEN
# $2 == org/repo
# $3 == release_version
# $4 == next_version

echo "Configuring git"
git config --global user.email "${GITHUB_ACTOR}@users.noreply.github.com"
git config --global user.name "${GITHUB_ACTOR}"
git fetch

echo -n "Determining target branch: "
target_branch=`cat $GITHUB_EVENT_PATH | jq '.release.target_commitish' | sed -e 's/^"\(.*\)"$/\1/g'`
echo $target_branch
git checkout $target_branch

echo "Setting release version in gradle.properties"
sed -i "s/^projectVersion.*$/projectVersion\=${3}/" gradle.properties
cat gradle.properties

echo "Pushing release version and recreating v${3} tag"
git add gradle.properties
git commit -m "Release v${3}"
git push origin $target_branch
git push origin :refs/tags/v${3}
git tag -fa v${3} -m "Release v${3}"
git push origin $target_branch --tags

echo "Closing again the release after updating the tag"
release_url=`cat $GITHUB_EVENT_PATH | jq '.release.url' | sed -e 's/^"\(.*\)"$/\1/g'`
echo $release_url
curl -s --request PATCH -H "Authorization: Bearer $1" -H "Content-Type: application/json" $release_url --data "{\"draft\": false}"