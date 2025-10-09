JAVAC        = javac
JAVA         = java
SRC          = src/main/java
BIN          = bin
MAIN_CLASS   = org.gargoyle.GargoyleGame
MODULEPATH   = /usr/share/openjfx/lib
ADD_MODULES  = --add-modules javafx.controls,javafx.graphics

# Sources Java
SOURCES      = $(shell find $(SRC) -type f -name "*.java" -print | sort)
MAIN_SRC     = $(SRC)/$(subst .,/,$(MAIN_CLASS)).java

.PHONY: all build run clean tree print doctor snapshot list restore help

all: run

build: $(BIN)/classes.stamp

$(BIN)/classes.stamp: $(SOURCES)
	@mkdir -p $(BIN)
	@echo "🛠️  Compilation..."
	$(JAVAC) --module-path $(MODULEPATH) $(ADD_MODULES) -d $(BIN) $(SOURCES)
	@touch $@

run: build
	@echo "🚀 Exécution..."
	$(JAVA) --module-path $(MODULEPATH) $(ADD_MODULES) -cp $(BIN) $(MAIN_CLASS)

clean:
	rm -rf $(BIN)

tree:
	@echo "📁 Arborescence
