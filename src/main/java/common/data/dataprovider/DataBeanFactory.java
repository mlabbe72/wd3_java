package common.data.dataprovider;


import common.data.testdata.IDataBean;

public class DataBeanFactory {

	@SuppressWarnings("unchecked")
	public static <T> T createLocalDataBean(IDataBean iDataBean) throws Exception {
		String id = iDataBean.getValue();
		Class<?> clazz = iDataBean.getClazz();
		YamlDataProvider iDataProvider = new YamlDataProvider();
		return (T) iDataProvider.getDataBean(clazz, id);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T createLocalJSONDataBean(IDataBean iDataBean) throws Exception {
		Class<?> clazz = iDataBean.getClazz();
		JSONDataProvider jsonDataProvider = new JSONDataProvider();
		return (T) jsonDataProvider.getJSONDataBean(clazz); 
	}

}
