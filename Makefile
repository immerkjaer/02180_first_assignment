clean:
	rm -rf classes/*
	rm -rf dumps

compile-game-files:
	javac -d "classes" -sourcepath 2048/ 2048/game/Game.java
	
compile-solver: compile-game-files
	javac -d "classes" -classpath "classes" -sourcepath 2048/ 2048/src/Main.java

cross-validation: clean compile-solver
	mkdir -p dumps
	touch dumps/validation.txt
	for vC in 0.02 0.035 0.07 ; do \
		for pC in 0.6 0.8 1.0 ; do \
			for gsC in 0.3 0.5 0.7 ; do \
				echo $$vC $$pC $$gsC ; \
				java -classpath "classes" src/Main \
					-vari $$vC \
					-place $$pC \
					-groupspread $$gsC \
					-empty 30 \
					-merge 7.5 \
					>> dumps/validation.txt ; \
   			done \
		done \
	done