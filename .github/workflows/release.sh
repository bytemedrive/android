#!/bin/bash

type=$1
branchName=$2
echo "Release type: $type on branch: $branchName"

git config --global user.name 'Github Actions'
git config --global user.email 'github.actions@users.noreply.github.com'

git fetch origin dev
git checkout dev
versionCode=$(./gradlew -q printVersionCode | tail -n 1)
git checkout $branchName

releaseVersion=$(./gradlew -q printReleaseVersion | tail -n 1)

releaseVersionMajor=$(echo $releaseVersion | cut -d. -f1)
releaseVersionMinor=$(echo $releaseVersion | cut -d. -f2)
releaseVersionPatch=$(echo $releaseVersion | cut -d. -f3)

if [[ $type = "major" ]]; then
    newMajor=$((releaseVersionMajor+1))
    newMinor=0
    newPatch=0
fi
if [[ $type = "minor" ]]; then
    newMajor=$releaseVersionMajor
    newMinor=$((releaseVersionMinor+1))
    newPatch=0
fi
if [[ $type = "patch" ]]; then
    newMajor=$releaseVersionMajor
    newMinor=$releaseVersionMinor
    newPatch=$((releaseVersionPatch+1))
fi

echo "New version $releaseVersion"
newSnapshot="${newMajor}.${newMinor}.${newPatch}-SNAPSHOT"
echo "New snapshot version $newSnapshot"

# Commit release
./gradlew setVersionName -PversionName=$releaseVersion
versionCode=$((versionCode + 1))
./gradlew setVersionCode -PversionCode=$versionCode
git add gradle.properties
git commit -m "move to release version $releaseVersion"
git tag -a "$releaseVersion" -m "version $releaseVersion"

# Create release branch
if [[ $type = "major" ]] || [[ $type = "minor" ]]; then
  releaseVersionWithoutPatch="${releaseVersionMajor}.${releaseVersionMinor}"

  git checkout -b release/$releaseVersionWithoutPatch
  ./gradlew setVersionName -PversionName="${releaseVersionWithoutPatch}.1-SNAPSHOT"
  versionCode=$((versionCode + 1))
  ./gradlew setVersionCode -PversionCode=$versionCode
  git add gradle.properties
  git commit -am 'release branch - move to SNAPSHOT version'
  git push --no-verify origin release/$releaseVersionWithoutPatch
  git checkout dev
fi

# Commit snapshot
./gradlew setVersionName -PversionName=$newSnapshot
versionCode=$((versionCode + 1))
./gradlew setVersionCode -PversionCode=$versionCode
git add gradle.properties
git commit -m "move to snapshot $newSnapshot"

git push origin $releaseVersion
git push origin $branchName

# Update version code in dev branch increased by patch release
if [[ $type = "patch" ]]; then
  git checkout dev
  ./gradlew setVersionCode -PversionCode=$versionCode
  git add gradle.properties
  git commit -m "Updated versionCode from new patch release"
  git push origin dev
fi

