/**Get request parameter processing
 * @param necessary Required
 * @param query Optional
 * @return {Object}
 */
export function reviseParam(necessary, query) {
    let params = arguments[0],
        querys = arguments[1],
        arr = [],
        str = '';
    for (var i in params) {
        arr.push(params[i])
    }
    str = arr.join('/');
    return { str, querys }
}

/**
 * format Timestamp example："2018-01-17 15:39:34"
 * @param d Timestamp
 * @param fmt "yyyy-MM-dd" || "yyyy-MM-dd HH:mm:ss"
 * @returns {string}
 */
export function format(d, fmt) {
    let date = {};
    if (!(d instanceof Date)) {
        date = new Date(parseInt(d));
    }
    let o = {
        "M+": date.getMonth() + 1, //month
        "d+": date.getDate(), //day 
        "H+": date.getHours(), //hour
        "m+": date.getMinutes(), //minute 
        "s+": date.getSeconds(), //second 
        "q+": Math.floor((date.getMonth() + 3) / 3), //quarter 
        "S": date.getMilliseconds() //millisecond
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}

/**
* number：formatted number
* decimals：Keep a few decimals
* dec_point：decimal point symbol
* thousands_sep：thousands of symbols
* */
export function numberFormat(number, decimals, dec_point, thousands_sep) {
    number = (number + '').replace(/[^0-9+-Ee.]/g, '');
    var n = !isFinite(+number) ? 0 : +number,
        prec = !isFinite(+decimals) ? 0 : Math.abs(decimals),
        sep = (typeof thousands_sep === 'undefined') ? ',' : thousands_sep,
        dec = (typeof dec_point === 'undefined') ? '.' : dec_point,
        s = '',
        toFixedFix = function (n, prec) {
            var k = Math.pow(10, prec);
            return '' + Math.ceil(n * k) / k;
        };

    s = (prec ? toFixedFix(n, prec) : '' + Math.round(n)).split('.');
    var re = /(-?\d+)(\d{3})/;
    while (re.test(s[0])) {
        s[0] = s[0].replace(re, "$1" + sep + "$2");
    }

    if ((s[1] || '').length < prec) {
        s[1] = s[1] || '';
        s[1] += new Array(prec - s[1].length + 1).join('0');
    }
    return s.join(dec);
}

export function formatData() {
    let newData = new Date();
    let Y = newData.getFullYear();
    let M = newData.getMonth() + 1 > 9 ? newData.getMonth() + 1 : "0" + (newData.getMonth() + 1);
    let D = newData.getDate() > 9 ? newData.getDate() : "0" + newData.getDate();
    let newdata = Y + "/" + M + "/" + D;
    return newdata
};