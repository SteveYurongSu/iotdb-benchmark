package cn.edu.tsinghua.iotdb.benchmark.conf;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.tsinghua.iotdb.benchmark.function.Function;
import cn.edu.tsinghua.iotdb.benchmark.function.FunctionParam;
import cn.edu.tsinghua.iotdb.benchmark.function.FunctionXml;

public class Config {
	private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
	
	public Config(){

	}

	public String host;
	public String port;

	/** 设备数量 */
	public int DEVICE_NUMBER = 2;
	/** 每个设备的传感器数量 */
	public int SENSOR_NUMBER = 5;
	/** 数据采集步长 */
	public long POINT_STEP = 7000;
	/** 数据发送缓存条数 */
	public int CACHE_NUM = 10;

	/**服务器性能监测模式*/
	public boolean SERVER_MODE = false;

	/** 文件的名字 */
	public String FILE_PATH ;
	/** 是否从文件读取数据*/
	public boolean READ_FROM_FILE = false;
	/** 一次插入到数据库的条数 */
	public int BATCH_OP_NUM = 100;

	public boolean TAG_PATH = true;
	public int STORE_MODE = 1;
	
	public long LOOP = 10000;
	
	/** 数据采集丢失率 */
	public double POINT_LOSE_RATIO = 0.01;
	// ============各函数比例start============//FIXME 传参数时加上这几个参数
	/** 线性 默认 9个 0.054 */
	public double LINE_RATIO = 0.054;
	/** 傅里叶函数 6个 0.036 */
	// public static double SIN_RATIO=0.386;//0.036
	public double SIN_RATIO = 0.036;// 0.036
	/** 方波 9个 0.054 */
	public double SQUARE_RATIO = 0.054;
	/** 随机数 默认 86个 0.512 */
	public double RANDOM_RATIO = 0.512;
	/** 常数 默认 58个 0.352 */
	// public static double CONSTANT_RATIO= 0.002;//0.352
	public double CONSTANT_RATIO = 0.352;// 0.352

	// ============各函数比例end============

	/** 内置函数参数 */
	public List<FunctionParam> LINE_LIST = new ArrayList<FunctionParam>();
	public List<FunctionParam> SIN_LIST = new ArrayList<FunctionParam>();
	public List<FunctionParam> SQUARE_LIST = new ArrayList<FunctionParam>();
	public List<FunctionParam> RANDOM_LIST = new ArrayList<FunctionParam>();
	public List<FunctionParam> CONSTANT_LIST = new ArrayList<FunctionParam>();
	/** 设备编号 */
	public List<String> DEVICE_CODES = new ArrayList<String>();
	/** 传感器编号 */
	public List<String> SENSOR_CODES = new ArrayList<String>();
	/** 设备_传感器 时间偏移量 */
	public Map<String, Long> SHIFT_TIME_MAP = new HashMap<String, Long>();
	/** 传感器对应的函数 */
	public Map<String, FunctionParam> SENSOR_FUNCTION = new HashMap<String, FunctionParam>();

	/** 历史数据开始时间 */
	public long HISTORY_START_TIME;
	/** 历史数据结束时间 */
	public long HISTORY_END_TIME;

	// 负载生成器参数 start
	/** LoadBatchId 批次id */
	public Long PERFORM_BATCH_ID;

	// 负载测试完是否删除数据
	public boolean IS_DELETE_DATA = false;
	public double WRITE_RATIO = 0.2;
	public double SIMPLE_QUERY_RATIO = 0.2;
	public double MAX_QUERY_RATIO = 0.2;
	public double MIN_QUERY_RATIO = 0.2;
	public double AVG_QUERY_RATIO = 0.2;
	public double COUNT_QUERY_RATIO = 0.2;
	public double SUM_QUERY_RATIO = 0.2;
	public double RANDOM_INSERT_RATIO = 0.2;
	public double UPDATE_RATIO = 0.2;

	public void updateLoadTypeRatio(double wr, double rir, double mqr, double sqr, double ur) {
		WRITE_RATIO = wr;
		RANDOM_INSERT_RATIO = rir;
		MAX_QUERY_RATIO = mqr;
		SIMPLE_QUERY_RATIO = sqr;
		UPDATE_RATIO = ur;
		if (WRITE_RATIO < 0 || RANDOM_INSERT_RATIO < 0 || MAX_QUERY_RATIO < 0 || SIMPLE_QUERY_RATIO < 0
				|| UPDATE_RATIO < 0) {
			LOGGER.error("some of load rate cannot less than 0");
			System.exit(0);
		}
		if (WRITE_RATIO == 0 && RANDOM_INSERT_RATIO == 0 && MAX_QUERY_RATIO == 0 && SIMPLE_QUERY_RATIO == 0
				&& UPDATE_RATIO == 0) {
			WRITE_RATIO = 0.2;
			SIMPLE_QUERY_RATIO = 0.2;
			MAX_QUERY_RATIO = 0.2;
			MIN_QUERY_RATIO = 0.2;
			AVG_QUERY_RATIO = 0.2;
			COUNT_QUERY_RATIO = 0.2;
			SUM_QUERY_RATIO = 0.2;
			RANDOM_INSERT_RATIO = 0.2;
			UPDATE_RATIO = 0.2;
		}
	}

	public void initInnerFucntion() {
		FunctionXml xml = null;
		try {
			InputStream input = Function.class.getResourceAsStream("function.xml");
			JAXBContext context = JAXBContext.newInstance(FunctionXml.class, FunctionParam.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			xml = (FunctionXml) unmarshaller.unmarshal(input);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		List<FunctionParam> xmlFuctions = xml.getFunctions();
		for (FunctionParam param : xmlFuctions) {
			if (param.getFunctionType().indexOf("_mono_k") != -1) {
				LINE_LIST.add(param);
			} else if (param.getFunctionType().indexOf("_mono") != -1) {
				// 如果min==max则为常数，系统没有非常数的
				if (param.getMin() == param.getMax()) {
					CONSTANT_LIST.add(param);
				}
			} else if (param.getFunctionType().indexOf("_sin") != -1) {
				SIN_LIST.add(param);
			} else if (param.getFunctionType().indexOf("_square") != -1) {
				SQUARE_LIST.add(param);
			} else if (param.getFunctionType().indexOf("_random") != -1) {
				RANDOM_LIST.add(param);
			}
		}
	}

	/**
	 * 初始化传感器函数 Constants.SENSOR_FUNCTION
	 */
	public void initSensorFunction() {
		// 根据传进来的各个函数比例进行配置
		double sumRatio = CONSTANT_RATIO + LINE_RATIO + RANDOM_RATIO + SIN_RATIO + SQUARE_RATIO;
		if (sumRatio != 0 && CONSTANT_RATIO >= 0 && LINE_RATIO >= 0 && RANDOM_RATIO >= 0 && SIN_RATIO >= 0
				&& SQUARE_RATIO >= 0) {
			double constantArea = CONSTANT_RATIO / sumRatio;
			double lineArea = constantArea + LINE_RATIO / sumRatio;
			double randomArea = lineArea + RANDOM_RATIO / sumRatio;
			double sinArea = randomArea + SIN_RATIO / sumRatio;
			double squareArea = sinArea + SQUARE_RATIO / sumRatio;
			Random r = new Random();
			for (int i = 0; i < SENSOR_NUMBER; i++) {
				double property = r.nextDouble();
				FunctionParam param = null;
				Random fr = new Random();
				double middle = fr.nextDouble();
				if (property >= 0 && property < constantArea) {// constant
					int index = (int) (middle * CONSTANT_LIST.size());
					param = CONSTANT_LIST.get(index);
				}
				if (property >= constantArea && property < lineArea) {// line
					int index = (int) (middle * LINE_LIST.size());
					param = CONSTANT_LIST.get(index);
				}
				if (property >= lineArea && property < randomArea) {// random
					int index = (int) (middle * RANDOM_LIST.size());
					param = RANDOM_LIST.get(index);
				}
				if (property >= randomArea && property < sinArea) {// sin
					int index = (int) (middle * SIN_LIST.size());
					param = SIN_LIST.get(index);
				}
				if (property >= sinArea && property < squareArea) {// square
					int index = (int) (middle * SQUARE_LIST.size());
					param = SQUARE_LIST.get(index);
				}
				if (param == null) {
					System.err.println(" initSensorFunction() 初始化函数比例有问题！");
					System.exit(0);
				}
				SENSOR_FUNCTION.put(SENSOR_CODES.get(i), param);
			}
		} else {
			System.err.println("function ration must >=0 and sum>0");
			System.exit(0);
		}
	}

	/**
	 * 根据传感器数，初始化传感器编号
	 * 
	 * @param sensorSum
	 * @return
	 */
	public List<String> initSensorCodes() {
		for (int i = 0; i < SENSOR_NUMBER; i++) {
			String sensorCode = "s_" + i;
			SENSOR_CODES.add(sensorCode);
		}
		return SENSOR_CODES;
	}

	/**
	 * 根据设备数，初始化设备编号
	 * 
	 * @param deviceSum
	 * @return
	 */
	public List<String> initDeviceCodes() {
		for (int i = 0; i < DEVICE_NUMBER; i++) {
			String deviceCode = "d_" + i;
			DEVICE_CODES.add(deviceCode);
		}
		return DEVICE_CODES;
	}

	public String getSensorCodeByRandom() {
		List<String> sensors = SENSOR_CODES;
		int size = sensors.size();
		Random r = new Random();
		return sensors.get(r.nextInt(size));
	}

	public String getDeviceCodeByRandom() {
		List<String> devices = DEVICE_CODES;
		int size = devices.size();
		Random r = new Random();
		return devices.get(r.nextInt(size));
	}

	public static void main(String[] args) {
		// Config config = Config.newInstance();

	}
}