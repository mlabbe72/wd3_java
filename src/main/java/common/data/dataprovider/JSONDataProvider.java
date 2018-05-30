package common.data.dataprovider;

import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import common.env.TestProperties;


public class JSONDataProvider {

	private static final String testDataFolderBase = "src/main/resources/testdata/";

	private static String testDataFolder;


	public <T> T getJSONDataBean(Class<T> clazz) throws Exception {
		String className = getRelativePath(clazz);
		String currentEnv = TestProperties.testProperties.getString(TestProperties.TEST_ENV);
		String userDirectory = System.getProperty("user.dir") + "/";
		String jsonFilePath = userDirectory + testDataFolder + currentEnv.toUpperCase() + "/" + className + ".json";
		byte[] encoded = Files.readAllBytes(Paths.get(jsonFilePath));
		String jsonData = new String(encoded, "UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JodaModule());
		return (T) mapper.readValue(jsonData, clazz);
	}

	private static String getRelativePath(@SuppressWarnings("rawtypes") Class clazz) {
		String className = clazz.getSimpleName();
		className = className.replace("common.data.beans.", "");
		className = className.replace("common.data.testdata.", "");
		className = className.toLowerCase().replace("bean", "");
		className = className.replace(".", "/");
		className = className.replace("[", "");
		className = className.replace("]", "");
		return className;
	}
}
