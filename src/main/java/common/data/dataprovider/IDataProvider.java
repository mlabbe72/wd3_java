package common.data.dataprovider;

import java.util.List;
import java.util.Map;

public interface IDataProvider {

	<T> List<T> getDataList(Class<T> clazz) throws Exception;
	
	public <T> Map<String, Object> getData(Class<T> clazz, String id) throws Exception;
	
}
