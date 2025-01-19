.PHONY: run test clean

ASMSRC  = ./asmsrc
CSRC    = ./csrc

test:
	@make -C $(ASMSRC) hex
	@make -C $(CSRC) hex
	@- sbt test
run:
	@mkdir -p build
	@sbt run

clean:
	@rm -rf build target project test_run_dir
	@make -C $(ASMSRC) clean
	@make -C $(CSRC) clean
