cross-validation:
	for v1 in 1 2 3 4 ; do \
		for v2 in 5 6 ; do \
			for v3 in 7 8 ; do \
				echo $$v1 $$v2 $$v3 ; \
   			done \
		done \
	done
