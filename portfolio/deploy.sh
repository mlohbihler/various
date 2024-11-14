npm run build
rm ../mlohbihler.github.io/assets/*
rm ../mlohbihler.github.io/index.html
cp -R dist/* ../mlohbihler.github.io/.
cd ../mlohbihler.github.io
git add .
git commit -m 'script deployment'
git push
