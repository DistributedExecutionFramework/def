using System;
using routine_api.routine;
using routine_api.exception;

namespace demoroutines
{
    public class PiCalc : ObjectiveRoutine<DEFDouble>
    {
        public static new void Main(String[] args)
        {
            AbstractRoutine.Main(args);
        }

        protected override DEFDouble Routine()
        {
            try
            {
                double start = GetParameter<DEFDouble>("start").Value;
                double end = GetParameter<DEFDouble>("end").Value;
                double stepSize = GetParameter<DEFDouble>("stepSize").Value;

                double sum = 0.0;
                for (double i = start; i < end; i++)
                {
                    double x = (i + 0.5) * stepSize;
                    sum += 4.0 / (1.0 + x * x);
                }

                sum *= stepSize;

                return new DEFDouble
                {
                    Value = sum
                };
            }
            catch (AccessParameterException e)
            {
                throw new RoutineException(e);
            }
        }
    }
}
