import unittest

from numpy import random
from pandas import DataFrame

from def_api.mapper import map_value, init_mapper
from def_api.ttypes import DEFDataFrame, DEFDataFrameList, DEFInteger, DEFDouble


class DEFMapperTest(unittest.TestCase):

    def setUp(self):
        init_mapper()

    def test_int(self):
        i = random.randint(0, 100)
        di = map_value(i)
        self.assertTrue(isinstance(di, DEFInteger))
        self.assertEqual(i, di.value)

    def test_float(self):
        f = random.random()
        df = map_value(f)
        self.assertTrue(isinstance(df, DEFDouble))
        self.assertEqual(f, df.value)

    def test_panda_data_frame(self):
        df_1 = DataFrame(random.randint(0, 100, size=(100, 4)), columns=list('ABCD'))
        df_2 = map_value(df_1)
        self.assertEqual(DEFDataFrame, type(df_2))
        df_3 = map_value(df_2)
        self.assertTrue(df_1.equals(df_3))

    def test_panda_data_frame_list(self):
        dfs_1 = []
        size = 10
        for i in range(0, size):
            dfs_1.append(DataFrame(random.randint(0, 10, size=(10, 2)), columns=list('XY')))
        dfs_2 = map_value(dfs_1)
        self.assertEqual(DEFDataFrameList, type(dfs_2))
        dfs_3 = map_value(dfs_2)
        for i in range(0, size):
            self.assertTrue(dfs_1[i].equals(dfs_3[i]))
