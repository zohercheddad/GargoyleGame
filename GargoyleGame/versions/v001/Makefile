JAVAC        = javac
JAVA         = java
SRC          = src/main/java
BIN          = bin
MAIN_CLASS   = org.gargoyle.GargoyleGame
MODULEPATH   = /usr/share/openjfx/lib
ADD_MODULES  = --add-modules javafx.controls,javafx.graphics

SOURCES      = $(shell find $(SRC) -name "*.java")

.PHONY: all build run clean tree

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
	@echo "📁 Arborescence du projet :"
	@find . -maxdepth 4 -type d -print | sed 's/^\.\///'
	
	
# --- Versioning snapshots ---
VERSIONS_DIR := versions

.PHONY: snapshot list restore

# make snapshot MSG="ajout obstacles + collisions AABB"
snapshot:
	@mkdir -p "$(VERSIONS_DIR)"
	@next=$$(printf "v%03d" $$(( $$(ls -1 "$(VERSIONS_DIR)" 2>/dev/null | grep -E '^v[0-9]{3}$$' | sed 's/^v//' | sort -n | tail -1 || echo 0) + 1 ))); \
	echo "📦 Création de $$next ..."; \
	mkdir -p "$(VERSIONS_DIR)/$$next"; \
	rsync -a --exclude 'bin' --exclude '$(VERSIONS_DIR)' --exclude '.git' ./ "$(VERSIONS_DIR)/$$next/"; \
	date +"%Y-%m-%d %H:%M:%S %Z" > "$(VERSIONS_DIR)/$$next/SNAPSHOT.txt"; \
	if [ -n "$$MSG" ]; then echo "$$MSG" >> "$(VERSIONS_DIR)/$$next/SNAPSHOT.txt"; fi; \
	echo "✅ Snapshot prêt : $(VERSIONS_DIR)/$$next"

# make list
list:
	@{ test -d "$(VERSIONS_DIR)" && ls -1 "$(VERSIONS_DIR)" | grep -E '^v[0-9]{3}$$' || echo "(aucune version encore)"; }

# make restore V=v002
restore:
	@test -n "$(V)" || { echo "❌ Usage: make restore V=v00X"; exit 1; }
	@test -d "$(VERSIONS_DIR)/$(V)" || { echo "❌ Version introuvable: $(VERSIONS_DIR)/$(V)"; exit 1; }
	@echo "↩️  Restauration depuis $(VERSIONS_DIR)/$(V) ... (vos fichiers actuels seront écrasés)"; \
	rsync -a --exclude '$(VERSIONS_DIR)' --exclude '.git' "$(VERSIONS_DIR)/$(V)/" ./; \
	echo "✅ Restauré depuis $(VERSIONS_DIR)/$(V)"
