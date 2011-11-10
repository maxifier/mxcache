package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.PublicAPI;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 10.11.11
 * Time: 13:19
 */
@PublicAPI
public class SchedulingConverter<F, T> implements MxConverter<F, Scheduled<F, T>> {
    private final MxConverter<F, T> converter;

    public SchedulingConverter(MxConverter<F, T> converter) {
        this.converter = converter;
    }

    private static class RealScheduled<F, T> implements Scheduled<F, T> {
        F nonConverted;
        T converted;

        public RealScheduled(F nonConverted) {
            this.nonConverted = nonConverted;
        }
    }

    @Override
    public Scheduled<F, T> convert(F t) throws ConverterException {
        RealScheduled<F, T> res = new RealScheduled<F, T>(t);
        CacheFactory.getScheduler().schedule(new ConverterRunnable<F, T>(res, converter));
        return res;
    }

    @PublicAPI
    public <R> MxConverter<Scheduled<T, R>, F> reverse(MxConverter<T, F> converter1, MxConverter<R, F> converter2) {
        return new ReverseConverter<F, T, R>(converter1, converter2);
    }

    @SuppressWarnings({"unchecked"})
    @PublicAPI
    public MxConverter<Scheduled<T, F>, F> reverse(MxConverter<T, F> converter) {
        return new ReverseConverter<F, T, F>(MxConverter.IDENTITY, converter);
    }

    private static class ReverseConverter<F, T, R> implements MxConverter<Scheduled<T, R>, F> {
        private final MxConverter<T, F> converter1;
        private final MxConverter<R, F> converter2;

        ReverseConverter(MxConverter<T, F> converter1, MxConverter<R, F> converter2) {
            this.converter1 = converter1;
            this.converter2 = converter2;
        }

        @Override
        public F convert(Scheduled<T, R> t) throws ConverterException {
            RealScheduled<T, R> scheduled = (RealScheduled<T, R>) t;

            T nonConverted;
            R converted;

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (scheduled) {
                nonConverted = scheduled.nonConverted;
                converted = scheduled.converted;
            }

            if (nonConverted == null) {
                return converter2.convert(converted);
            }
            return converter1.convert(nonConverted);
        }
    }

    private static class ConverterRunnable<F, T> implements Runnable {
        private final RealScheduled<F, T> res;
        private final MxConverter<F, T> converter;

        public ConverterRunnable(RealScheduled<F, T> res, MxConverter<F, T> converter) {
            this.res = res;
            this.converter = converter;
        }

        @Override
        public void run() {
            T converted = converter.convert(res.nonConverted);
            synchronized (res) {
                res.nonConverted = null;
                res.converted = converted;
            }
        }
    }
}
