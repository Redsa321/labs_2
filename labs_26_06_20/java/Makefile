SOURCES := $(wildcard src/*.java)
CLASSES := build/classes
JAVAFX := javafx.controls

.PHONY: all compile server client docs clean

all: compile

compile: $(CLASSES)/.stamp

$(CLASSES)/.stamp: $(SOURCES)
	mkdir -p $(CLASSES)
	javac -encoding UTF-8 --add-modules $(JAVAFX) \
		-d $(CLASSES) $(SOURCES)
	touch $@

server: compile
	java -cp $(CLASSES) TreeServer $(PORT)

client: compile
	java --add-modules $(JAVAFX) \
		--enable-native-access=javafx.graphics \
		-cp $(CLASSES) TreeClient

docs: compile
	mkdir -p build/docs/javadoc
	javadoc -quiet -Xdoclint:none -encoding UTF-8 \
		--add-modules $(JAVAFX) -classpath $(CLASSES) \
		-d build/docs/javadoc $(SOURCES)

clean:
	rm -rf build
