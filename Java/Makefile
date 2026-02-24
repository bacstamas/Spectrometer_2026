# --- Variables ---
JAVAC        = javac
JAR          = jar
SRC_DIR      = src
LIB_DIR      = lib
BIN_DIR      = bin
TEMP_DIR     = temp
MAIN_JAR     = Main.jar
MANIFEST     = manifest.txt

# Classpath for compilation (includes all jars in lib)
# Note: Use ":" for Linux/macOS and ";" for Windows
CP           = "$(LIB_DIR)/*"

# Find all .java files in the src directory
SOURCES      = $(wildcard $(SRC_DIR)/*.java)

# --- Targets ---

.PHONY: all clean run compile unpack

all: $(MAIN_JAR)

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

# Clean build artifacts
clean:
	@echo "Cleaning..."
	rm -rf $(BIN_DIR) $(TEMP_DIR) $(MAIN_JAR)

# Build and run the project
run: all
	@echo "Running project..."
	java -jar $(MAIN_JAR)