import axios from 'axios';

// 基础币种（数据库存储的币种）
export const BASE_CURRENCY = 'CNY';

// 支持的币种列表
export const SUPPORTED_CURRENCIES = [
    { code: 'CNY', symbol: '¥', name: '人民币' },
    { code: 'USD', symbol: '$', name: '美元' },
    { code: 'GBP', symbol: '£', name: '英镑' },
    { code: 'EUR', symbol: '€', name: '欧元' },
    { code: 'HKD', symbol: 'HK$', name: '港币' },
    { code: 'JPY', symbol: '¥', name: '日元' },
    { code: 'KRW', symbol: '₩', name: '韩元' },
    { code: 'CAD', symbol: 'C$', name: '加元' },
    { code: 'AUD', symbol: 'A$', name: '澳元' },
    { code: 'SGD', symbol: 'S$', name: '新币' }
];

// 缓存汇率数据，避免频繁请求 API
let ratesCache = {
    data: null,
    timestamp: 0
};

// 缓存有效期：1小时
const CACHE_DURATION = 3600 * 1000;

/**
 * 获取实时汇率数据 (使用 ExchangeRate-API 的免费接口，无需 API Key)
 */
export const fetchRates = async () => {
    if (import.meta.env.VITE_DEMO_MODE === 'true') return { CNY: 1, USD: 0.14, GBP: 0.11, EUR: 0.13, HKD: 1.09, JPY: 20.5, KRW: 185, CAD: 0.19, AUD: 0.21, SGD: 0.19 };
    const now = Date.now();
    if (ratesCache.data && (now - ratesCache.timestamp < CACHE_DURATION)) {
        return ratesCache.data;
    }

    try {
        // 使用公开免费 API
        const response = await axios.get(`https://open.er-api.com/v6/latest/${BASE_CURRENCY}`);
        if (response.data && response.data.result === 'success') {
            ratesCache = {
                data: response.data.rates,
                timestamp: now
            };
            return response.data.rates;
        }
    } catch (error) {
        console.error('获取汇率失败:', error);
        // 如果失败，返回一组基础汇率作为兜底（2024年初大致汇率）
        return {
            'CNY': 1,
            'USD': 0.14,
            'GBP': 0.11,
            'EUR': 0.13,
            'HKD': 1.09,
            'JPY': 20.5,
            'KRW': 185,
            'CAD': 0.19,
            'AUD': 0.21,
            'SGD': 0.19
        };
    }
    return ratesCache.data;
};

/**
 * 转换金额
 * @param {number} amount 原始金额 (CNY)
 * @param {string} targetCurrency 目标币种代码
 * @param {object} rates 汇率表
 */
export const convertPrice = (amount, targetCurrency, rates) => {
    if (!amount || isNaN(amount)) return 0;
    if (targetCurrency === BASE_CURRENCY || !rates) return amount;
    
    const rate = rates[targetCurrency];
    if (!rate) return amount;
    
    return (amount * rate).toFixed(2);
};

/**
 * 获取币种符号
 */
export const getCurrencySymbol = (code) => {
    const curr = SUPPORTED_CURRENCIES.find(c => c.code === code);
    return curr ? curr.symbol : '¥';
};
