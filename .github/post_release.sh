#!/bin/bash
# $1 == GH_TOKEN
# $2 == org/repo
# $3 == release_version
# $4 == next_version

echo -n "Retrieving current milestone number: "
milestone_number=`curl -s https://api.github.com/repos/$2/milestones | jq -c ".[] | select (.title == \"$3\") | .number" | sed -e 's/"//g'`
echo $milestone_number

echo "Closing current milestone"
curl -s --request PATCH -H "Authorization: Bearer $1" -H "Content-Type: application/json" https://api.github.com/repos/$2/milestones/$milestone_number --data '{"state":"closed"}'

# echo "Getting issues closed"
# issues_closed=`curl -s "https://api.github.com/repos/$2/issues?milestone=$milestone_number" | jq '.[] | "* \(.title) (#\(.number))"' | sed -e 's/^"\(.*\)"$/\1/g'`
# echo $issues_closed

# echo -n "Getting release url: "
# release_url=`cat $GITHUB_EVENT_PATH | jq '.release.url' | sed -e 's/^"\(.*\)"$/\1/g'`
# echo $release_url

# echo -n "Getting release body: "
# release_body=`cat $GITHUB_EVENT_PATH | jq '.release.body' | sed -e 's/^"\(.*\)"$/\1/g'`
# echo $release_body

# echo -n "Updating release body: "
# release_body="${release_body}\r\n${issues_closed}"
# echo $release_body
# curl -i --request PATCH -H "Authorization: Bearer $1" -H "Content-Type: application/json" $release_url --data "{\"body\": \"$release_body\"}"

echo "Creating new milestone"
curl -s --request POST -H "Authorization: Bearer $1" -H "Content-Type: application/json" "https://api.github.com/repos/$2/milestones" --data "{\"title\": \"$4\"}"

echo "Setting new snapshot version"
sed -i "s/^projectVersion.*$/projectVersion\=${4}-SNAPSHOT/" gradle.properties
cat gradle.properties

echo "Committing and pushing"
git config --global user.email "${GITHUB_ACTOR}@users.noreply.github.com"
git config --global user.name "${GITHUB_ACTOR}"
git fetch
git checkout master
git add gradle.properties 
git commit -m "Back to snapshot"
git push origin master