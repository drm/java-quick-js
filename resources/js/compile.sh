for f in *.js; do
	/home/gerard/git/bellard/quickjs/qjsc -c $f -o $(basename $f .js).c
done

