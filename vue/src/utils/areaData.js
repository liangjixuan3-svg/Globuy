import { Country, State, City } from 'country-state-city';
import { regionData } from 'element-china-area-data';

// 定义主流国家代码 (不再包含 CN，因为 CN 将单独处理)
const MAINSTREAM_COUNTRIES = ['HK', 'MO', 'US', 'GB', 'CA', 'AU', 'JP', 'KR', 'DE', 'FR', 'SG'];

// 国家/地区代码到中文名称的映射
const COUNTRY_NAME_MAP = {
    'HK': '中国香港',
    'MO': '中国澳门',
};

/**
 * 处理中国数据：封装为四级结构 (中国 -> 省 -> 市 -> 区)
 * 直接使用 element-china-area-data 的原始中文数据
 */
const chinaData = {
    value: '中国',
    label: '中国',
    children: regionData.map(province => ({
        value: province.label,
        label: province.label,
        children: province.children ? province.children.map(city => ({
            value: city.label,
            label: city.label,
            children: city.children ? city.children.map(district => ({
                value: district.label,
                label: district.label
            })) : undefined
        })) : undefined
    }))
};

/**
 * 获取全球主流国家的级联数据 (国家 -> 省/州 -> 城市)
 * 国际数据目前通常到城市级别
 */
const internationalData = MAINSTREAM_COUNTRIES.map(countryCode => {
    const country = Country.getCountryByCode(countryCode);
    const states = State.getStatesOfCountry(countryCode);
    
    return {
        value: COUNTRY_NAME_MAP[countryCode] || country.name,
        label: COUNTRY_NAME_MAP[countryCode] || country.name,
        children: states.map(state => {
            const cities = City.getCitiesOfState(countryCode, state.isoCode);
            return {
                value: state.name,
                label: state.name,
                children: cities.length > 0 ? cities.map(city => ({
                    value: city.name,
                    label: city.name
                })) : undefined
            };
        })
    };
});

// 合并数据，确保中国排在第一位
export const worldRegionData = [chinaData, ...internationalData];
