

# II Git
## 2.1 Often used
* shortcut (XCode): control + V open command line
```
git add .
git commit --amend
git push origin ft-wong -f

git add .
git commit -am "0.0.1 | "
git push origin ft-wong

git checkout dev
git merge --no-ff ft-wong 
git merge --no-ff origin/feature-mia

git push origin dev

git checkout main
git merge --no-ff dev

git push origin main
git tag -a v0.0.1 -m 'version 0.0.1'

git checkout ft-wong
git pull origin main
```