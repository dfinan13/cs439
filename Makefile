OUTPUT_PATH=outputs_pt

clean:
	rm config_[1-6]_v.txt config_[1-6]_vv.txt

classes:
	javac -cp src -d bin src/cpu/CPU.java src/os/OS.java

configs: config1 config2 config3 config4 config5 config6

config1: classes
	java -cp bin simulator.Simulator -v config_1 > config_1_v.txt
	diff $(OUTPUT_PATH)/config_1_v.txt config_1_v.txt

config2: classes
	java -cp bin simulator.Simulator -v config_2 > config_2_v.txt
	diff $(OUTPUT_PATH)/config_2_v.txt config_2_v.txt

config3: classes
	java -cp bin simulator.Simulator -v config_3 > config_3_v.txt
	diff $(OUTPUT_PATH)/config_3_v.txt config_3_v.txt

config4: classes
	java -cp bin simulator.Simulator -v config_4 > config_4_v.txt
	diff $(OUTPUT_PATH)/config_4_v.txt config_4_v.txt

config5: classes
	java -cp bin simulator.Simulator -v config_5 > config_5_v.txt
	diff $(OUTPUT_PATH)/config_5_v.txt config_5_v.txt

config6: classes
	java -cp bin simulator.Simulator -v config_6 > config_6_v.txt
	diff $(OUTPUT_PATH)/config_6_v.txt config_6_v.txt

configsvv: config1vv config2vv config3vv config4vv config5vv config6vv

config1vv: classes
	java -cp bin simulator.Simulator -vv config_1 > config_1_vv.txt
	diff $(OUTPUT_PATH)/config_1_vv.txt config_1_vv.txt

config2vv: classes
	java -cp bin simulator.Simulator -vv vconfig_2 > config_2_vv.txt
	diff $(OUTPUT_PATH)/config_2_vv.txt config_2_vv.txt

config3vv: classes
	java -cp bin simulator.Simulator -vv config_3 > config_3_vv.txt
	diff $(OUTPUT_PATH)/config_3_vv.txt config_3_vv.txt

config4vv: classes
	java -cp bin simulator.Simulator -vv config_4 > config_4_vv.txt
	diff $(OUTPUT_PATH)/config_4_vv.txt config_4_v.txt

config5vv: classes
	java -cp bin simulator.Simulator -vv config_5 > config_5_vv.txt
	diff $(OUTPUT_PATH)/config_5_vv.txt config_5_v.txt

config6vv: classes
	java -cp bin simulator.Simulator -vv config_6 > config_6_vv.txt
	diff $(OUTPUT_PATH)/config_6_vv.txt config_6_v.txt

turnin_setup:
	tar -cvf proj3_`whoami`.tar.gz README src/os/OS.java src/cpu/CPU.java

turnin: turnin_setup
	turnin --submit yjkwon proj3_rockhold proj3_`whoami`.tar.gz

