// version 0.1.0

(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        // AMD. Register as an anonymous module.
        define([], factory);
    } else if (typeof module === 'object' && module.exports) {
        // Node. Does not work with strict CommonJS, but
        // only CommonJS-like environments that support module.exports,
        // like Node.
        module.exports = factory();
    } else {
        // Browser globals (root is window)
        root.ytMath = factory();
    }
} (this, function () {
   
    // 加法
    function ytAdd(arg1, arg2) {
        var r1, r2, m, c;

        try {
            r1 = arg1.toString().split(".")[1].length;
        }
        catch (e) {
            r1 = 0;
        }

        try {
            r2 = arg2.toString().split(".")[1].length;
        }
        catch (e) {
            r2 = 0;
        }

        c = Math.abs(r1 - r2);
        m = Math.pow(10, Math.max(r1, r2));
        if (c > 0) {
            var cm = Math.pow(10, c);
            if (r1 > r2) {
                arg1 = Number(arg1.toString().replace(".", ""));
                arg2 = Number(arg2.toString().replace(".", "")) * cm;
            } else {
                arg1 = Number(arg1.toString().replace(".", "")) * cm;
                arg2 = Number(arg2.toString().replace(".", ""));
            }
        } else {
            arg1 = Number(arg1.toString().replace(".", ""));
            arg2 = Number(arg2.toString().replace(".", ""));
        }
        
        return (arg1 + arg2) / m;
    }

    // 减法
    function ytSub(arg1, arg2) {
        return ytAdd(arg1, -arg2);
    }

    // 乘法
    function ytMul(arg1, arg2) {
        var m = 0, s1 = arg1.toString(), s2 = arg2.toString();

        try {
            m += s1.split(".")[1].length;
        }
        catch (e) {
        }

        try {
            m += s2.split(".")[1].length;
        }
        catch (e) {
        }

        return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m);
    }


    // 除法
    function ytDiv(arg1, arg2) {
        var t1 = 0, t2 = 0, r1, r2;

        try {
            t1 = arg1.toString().split(".")[1].length;
        }
        catch (e) {
        }

        try {
            t2 = arg2.toString().split(".")[1].length;
        }
        catch (e) {
        }

        r1 = Number(arg1.toString().replace(".", ""));
        r2 = Number(arg2.toString().replace(".", ""));

        return (r1 / r2) * Math.pow(10, t2 - t1);
    }

    return {
        add: ytAdd,
        sub: ytSub,
        mul: ytMul,
        div: ytDiv
    }
}));
