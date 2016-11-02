function getDesc(data, key) {
		if(!data){
			return key
		}
        return data[key] === undefined ? key : data[key]

}

export default getDesc