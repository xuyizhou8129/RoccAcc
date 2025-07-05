SHELL=/bin/bash

ifndef RISCV
$(error RISCV is unset. You must set RISCV yourself, or through the Chipyard auto-generated env file)
else
$(info Running with RISCV=$(RISCV))
endif

XLEN ?= 64
NHARTS ?= 1

basedir := $(abspath .)

RISCV_PREFIX := riscv64-unknown-elf

CC :=  $(RISCV_PREFIX)-gcc
OBJDUMP := $(RISCV_PREFIX)-objdump

RISCV_GCC_OPTS ?= -DPREALLOCATE=1 -static -std=gnu99 -fno-common -fno-builtin-printf -Wall -fvisibility=hidden -nostdlib -nostartfiles

TEST_SRCS :=
include src/modules.mk
INCLUDE_DIRS := $(abspath ./include)
BUILD_DIR := build
TEST_BIN_DIR := bin

OBJS = $(addprefix $(BUILD_DIR)/,$(TEST_SRCS:%.c=%.o))
TEST_BINS := $(addprefix $(TEST_BIN_DIR)/,$(TEST_SRCS:%.c=%))
SIM_BINS := $(addprefix $(TEST_BIN_DIR)/,$(TEST_SRCS:%.c=%.riscv))

INCLUDE_FLAGS := -I$(INCLUDE_DIRS)

CFLAGS := -g -D__riscv_xlen=$(XLEN) -mcmodel=medany
CFLAGS += $(INCLUDE_FLAGS)

SIM_LDSCRIPT := $(abspath utils/link.ld)
LDFLAGS := -static -nostdlib -nostartfiles -lgcc -T$(SIM_LDSCRIPT)
include utils/Makefile
UTIL_OBJS := $(addprefix $(BUILD_DIR)/,$(UTIL_OBJS))
OBJS += $(UTIL_OBJS)

.PHONY: build

build: $(OBJS) $(SIM_BINS) $(TEST_BINS)

bin/%.riscv: $(BUILD_DIR)/%.o $(OBJS) $(LDSCRIPT)
	@echo "Linking test binaries for baremetal"
	@mkdir -p bin
	$(CC) $(CFLAGS) $(RISCV_GCC_OPTS) $(LDFLAGS) -o $@ $< $(UTIL_OBJS)

bin/%: $(BUILD_DIR)/%.o $(OBJS)
	@echo "Linking test binaries for ELF"
	@mkdir -p bin
	$(CC) $(CFLAGS) -o $@ $<

$(BUILD_DIR)/%.o: src/%.c
	@mkdir -p build
	$(CC) $(CFLAGS) -c $< -o $@

$(BUILD_DIR)/%.o: utils/%.S
	$(CC) $(CFLAGS) -D__ASSEMBLY__=1 -o $@ -c $<

$(BUILD_DIR)/%.o: utils/%.c
	$(CC) $(CFLAGS) -o $@ -c $<

clean:
	rm -rf $(TEST_BIN_DIR) $(BUILD_DIR) utils/*.o src/*.o
