# --- Variables ---
JAVAC        = javac
JAR          = jar
SRC_DIR      = src
LIB_DIR      = lib
BIN_DIR      = bin
TEMP_DIR     = temp
MAIN_JAR     = Main.jar
MANIFEST     = manifest.txt

MAIN_CLASS   = Main
DIST_DIR     = dist
APP_NAME     = SpectroMeter
ICON_PATH     = ../Docs/Figures/spectro.png

# Classpath for compilation (includes all jars in lib)
# Note: Use ":" for Linux/macOS and ";" for Windows
CP           = "$(LIB_DIR)/*"

# Find all .java files in the src directory
SOURCES = $(shell find $(SRC_DIR) -name "*.java")

# --- Targets ---

.PHONY: all clean run compile unpack

all: $(MAIN_JAR) installer

# 1. Compile the Java source files
compile:
	@echo "Compiling..."
	@mkdir -p $(BIN_DIR)
	$(JAVAC) -cp $(CP) -d $(BIN_DIR) $(SOURCES)

# 2. Unpack libraries to include them in the Fat JAR
unpack:
	@echo "Unpacking libraries..."
	@mkdir -p $(TEMP_DIR)
	@cd $(TEMP_DIR) && for f in ../$(LIB_DIR)/*.jar; do $(JAR) xf $$f; done

# 3. Create the final executable Fat JAR
$(MAIN_JAR): compile unpack
	@echo "Building Fat JAR..."
	$(JAR) cfm $(MAIN_JAR) $(MANIFEST) -C $(BIN_DIR) . -C $(TEMP_DIR) .
	@echo "Build successful: $(MAIN_JAR)"

# 4. Create an installer
installer: $(MAIN_JAR)
	@mkdir -p $(DIST_DIR)
	@cp $(MAIN_JAR) $(DIST_DIR)/
	@cp -r $(LIB_DIR) $(DIST_DIR)/
	@jpackage \
		--name $(APP_NAME)\
		--input $(DIST_DIR)\
		--main-jar $(MAIN_JAR)\
		--main-class $(MAIN_CLASS)\
		--type deb\
		--dest $(DIST_DIR)/installer \
	  	--java-options "-Djava.awt.headless=false"\
	  	--icon $(ICON_PATH)

# Clean build artifacts
clean:
	@echo "Cleaning..."
	rm -rf $(BIN_DIR) $(TEMP_DIR) $(MAIN_JAR) $(DIST_DIR)

# Build and run the project
run: $(MAIN_JAR)
	@echo "Running project..."
	java -jar $(MAIN_JAR)
