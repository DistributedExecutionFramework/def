using routine_api;
using routine_api.exception;

namespace demo_routines
{
    public class PiCalc : ObjectiveRoutine<DEFDouble>
    {
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