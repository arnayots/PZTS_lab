import time

import numpy as np

logfile = "latest.log"


class calc():
    sygma = 1
    ro = np.NAN
    beta = np.NAN
    x0 = np.NAN
    y0 = np.NAN
    z0 = np.NAN
    T = -1
    f_log = open(logfile, "w")
    tDistr = np.empty([])
    n_pts = 100
    # processed_attr = 0

    attr_x = []
    attr_y = []
    attr_z = []
    sens_u1 = []
    sens_u2 = []
    sens_u3 = []

    u1_st = 1
    u2_st = 1
    u3_st = 1

    def __init__(self, sygma_, ro_, beta_, x0_, y0_, z0_, T_):
        self.start_log()
        self.sygma = sygma_
        self.ro = ro_
        self.beta = beta_
        self.x0 = x0_
        self.y0 = y0_
        self.z0 = z0_
        self.T = T_
        self.log("Object inited")
        self.log("variables: sygma=" +str(self.sygma) + " ro=" +str(self.ro) + " beta=" +str(self.beta) + " x0=" +str(self.x0) + " y0=" +str(self.y0) + " z0=" +str(self.z0) + " T=" +str(self.T))

    def set_N(self, n):
        self.n_pts = n

    def generateTDistribution(self):
        self.tDistr = np.linspace(0, self.T, self.n_pts)

    def processAttrInTDist(self):
        self.attr_x = []
        self.attr_y = []
        self.attr_z = []
        self.log("start attractor trajectory calculation time=" + str(time.clock()))
        self.attr_x.append(self.x0)
        self.attr_y.append(self.y0)
        self.attr_z.append(self.z0)
        for i in range(1, self.n_pts):
            x = self.attr_x[i - 1]
            y = self.attr_y[i - 1]
            z = self.attr_z[i - 1]
            self.attr_x.append(self.calc_new_x(x, y, z, i))
            self.attr_y.append(self.calc_new_y(x, y, z, i))
            self.attr_z.append(self.calc_new_z(x, y, z, i))
            # i_tmp = i
        self.log("attractor trajectory calculation stopped time=" + str(time.clock()))
        # print(self.attr_x)
        # print(self.attr_y)
        # print(self.attr_z)

    def processSensivity(self):
        self.sens_u1 = []
        self.sens_u2 = []
        self.sens_u3 = []
        self.log("start attractor sensitivity calculation time=" + str(time.clock()))
        self.sens_u1.append(self.u1_st)
        self.sens_u2.append(self.u2_st)
        self.sens_u3.append(self.u3_st)
        for i in range(1, self.n_pts):
            u1 = self.sens_u1[i - 1]
            u2 = self.sens_u2[i - 1]
            u3 = self.sens_u3[i - 1]
            self.sens_u1.append(self.calc_new_u1(u1, u2, u3, i))
            self.sens_u2.append(self.calc_new_u2(u1, u2, u3, i))
            self.sens_u3.append(self.calc_new_u3(u1, u2, u3, i))
        self.log("attractor sensitivity calculation stopped time=" + str(time.clock()))

    def getStrAttractorData(self):
        res = ""
        t_str = "attr_t="
        x_str = "attr_x="
        y_str = "attr_y="
        z_str = "attr_z="
        t_str += str(self.tDistr[0])
        x_str += str(self.attr_x[0])
        y_str += str(self.attr_y[0])
        z_str += str(self.attr_z[0])
        for i in range(1, self.n_pts):
            t_str += " " + str(self.tDistr[i])
            x_str += " " + str(self.attr_x[i])
            y_str += " " + str(self.attr_y[i])
            z_str += " " + str(self.attr_z[i])
        res += t_str + "&" + x_str + "&" + y_str + "&" + z_str
        self.log("attractor trajectory data has been converted to string =>")
        self.log(res)
        return res

    def getStrSensetivityData(self):
        res = ""
        t_str = "sens_t="
        u1_str = "sens_u1="
        u2_str = "sens_u2="
        u3_str = "sens_u3="
        t_str += str(self.tDistr[0])
        u1_str += str(self.sens_u1[0])
        u2_str += str(self.sens_u2[0])
        u3_str += str(self.sens_u3[0])
        for i in range(1, self.n_pts):
            t_str += " " + str(self.tDistr[i])
            u1_str += " " + str(self.sens_u1[i])
            u2_str += " " + str(self.sens_u2[i])
            u3_str += " " + str(self.sens_u3[i])
        res += t_str + "&" + u1_str + "&" + u2_str + "&" + u3_str
        self.log("attractor sensivity data has been converted to string =>")
        self.log(res)
        return res

    def fu1(self, u1, u2, u3, t_ind):
        return self.sygma * (-u1 + u2)

    def fu2(self, u1, u2, u3, t_ind):
        return (self.ro - self.attr_z[t_ind]) * u1 - u2 - self.attr_x[t_ind] * u3

    def fu3(self, u1, u2, u3, t_ind):
        return self.attr_y[t_ind] * u1 + self.attr_x[t_ind] * u2 - self.beta * u3

    def calc_new_u1(self, u1, u2, u3, t_ind):
        return u1 + ( self.fu1(u1, u2, u3, t_ind) * (self.tDistr[t_ind] - self.tDistr[t_ind - 1]))

    def calc_new_u2(self, u1, u2, u3, t_ind):
        return u2 + ( self.fu2(u1, u2, u3, t_ind) * (self.tDistr[t_ind] - self.tDistr[t_ind - 1]))

    def calc_new_u3(self, u1, u2, u3, t_ind):
        return u3 + ( self.fu3(u1, u2, u3, t_ind) * (self.tDistr[t_ind] - self.tDistr[t_ind - 1]))

    def fx(self, x, y, z):
        return self.sygma * (y - x)

    def fy(self, x, y, z):
        return x * (self.ro - z) - y

    def fz(self, x, y, z):
        return x * y - self.beta * z

    def calc_new_x(self, x, y, z, t_ind):
        return x + ( self.fx(x, y, z) * (self.tDistr[t_ind] - self.tDistr[t_ind - 1]))

    def calc_new_y(self, x, y, z, t_ind):
        return y + ( self.fy(x, y, z) * (self.tDistr[t_ind] - self.tDistr[t_ind - 1]))

    def calc_new_z(self, x, y, z, t_ind):
        return z + ( self.fz(x, y, z) * (self.tDistr[t_ind] - self.tDistr[t_ind - 1]))

    def start_log(self):
        self.f_log = open(logfile, "w")
        self.log("log startted " + str(time.ctime()))

    def stop_log(self):
        self.log("log stopped " + str(time.clock()) + " " + str(time.ctime()))
        self.f_log.close()

    def log(self, msg):
        self.f_log.write("$ " + str(msg) + "\n")


