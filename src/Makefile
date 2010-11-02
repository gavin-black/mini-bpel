all: parse evaluation operations service calls database
	javac bpel/*java

evaluation:
	javac bpel/evaluation/*java

database:
	javac bpel/database/*java

parse: 
	javac bpel/parse/*java

operations:
	javac bpel/operations/*java

service:
	javac bpel/service/*java

calls:
	javac bpel/calls/*java

tomcat: all
	jar -cvf bpel.jar bpel/; mv bpel.jar ../tomcat_dist/WEB-INF/lib/; cd ../tomcat_dist && jar -cvf mini_bpel.war . && mv mini_bpel.war ..

clean: clean_vim clean_class
	# Complete 

clean_vim: 
	rm -f *~ bpel/*~ bpel/parse/*~ bpel/operations/*~ bpel/service/*~ bpel/database/*~ bpel/calls/*~ scripts/*~ bpel/evaluation/*~;

clean_class:
	rm -f bpel/*class bpel/parse/*class bpel/operations/*class bpel/service/*class bpel/calls/*class bpel/database/*class bpel/evaluation/*class; rm ../tomcat_dist/mini_bpel.war
