#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
	openssl aes-256-cbc -K $encrypted_89f01b983058_key -iv $encrypted_89f01b983058_iv -in codesigning.asc.enc -out cd/codesigning.asc -d
	gpg --fast-import cd/signingkey.asc
fi