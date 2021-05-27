import argparse
import math
import sys
import traceback

from calc import calc

error_msg = ""

debug = True
debug = False

def equals(x, y):
    return abs(x - y) < 1E-7

def createParser():
    parser = argparse.ArgumentParser()
    parser.add_argument('--sygma_top', type=float)
    parser.add_argument('--sygma_bottom', type=float)
    parser.add_argument('--ro_top', type=float)
    parser.add_argument('--ro_bottom', type=float)
    parser.add_argument('--beta_top', type=float)
    parser.add_argument('--beta_bottom', type=float)
    parser.add_argument('--x0', type=float)
    parser.add_argument('--y0', type=float)
    parser.add_argument('--z0', type=float)
    parser.add_argument('--t_final', type=float)
    parser.add_argument('--n_pts', type=int)
    return parser

def checkNamespace(namesp: argparse.Namespace):
    if namesp.sygma_top == None:
        print("error: no parameters for " + "sygma_top\n")
    if namesp.sygma_bottom == None:
        print("error: no parameters for " + "sygma_bottom\n")
    if namesp.ro_top == None:
        print("error: no parameters for " + "ro_top\n")
    if namesp.ro_bottom == None:
        print("error: no parameters for " + "ro_bottom\n")
    if namesp.beta_top == None:
        print("error: no parameters for " + "beta_top\n")
    if namesp.beta_bottom == None:
        print("error: no parameters for " + "beta_bottom\n")
    if namesp.x0 == None:
        print("error: no parameters for " + "x0\n")
    if namesp.y0 == None:
        print("error: no parameters for " + "y0\n")
    if namesp.z0 == None:
        print("error: no parameters for " + "z0\n")
    if namesp.t_final == None:
        print("error: no parameters for " + "t_final\n")

def doParse(parser: argparse.ArgumentParser):
    if debug:
        print(sys.argv)
        print("Parsing :")
    namespace = parser.parse_args()
    if debug:
        print(namespace)
    checkNamespace(namespace)
    return namespace

if __name__ == '__main__':
    try:
        parser = createParser()
        ns = doParse(parser)
        if error_msg != "":
            print("error_msg=" + error_msg)
            sys.exit(-2)

        sygma_top = ns.sygma_top
        sygma_bottom = ns.sygma_bottom
        ro_top = ns.ro_top
        ro_bottom = ns.ro_bottom
        beta_top = ns.beta_top
        beta_bottom = ns.beta_bottom
        x0 = ns.x0
        y0 = ns.y0
        z0 = ns.z0
        t_final = ns.t_final
        n_pts = 100
        if ns.n_pts != None:
            n_pts = ns.n_pts

        if equals(sygma_bottom, 0):
            error_msg += "sygma_bottom equals 0"
        if equals(ro_bottom, 0):
            error_msg += "ro_bottom equals 0"
        if equals(beta_bottom, 0):
            error_msg += "beta_bottom equals 0"
        if error_msg != "":
            print("error_msg=" + error_msg)
            sys.exit(-2)

        tmp = calc(sygma_top/sygma_bottom, ro_top/ro_bottom, beta_top/beta_bottom, x0, y0, z0, t_final)
        if ns.n_pts != None:
            tmp.set_N(n_pts)
        tmp.generateTDistribution()
        tmp.processAttrInTDist()
        msg1 = tmp.getStrAttractorData()
        tmp.processSensivity()
        msg2 = tmp.getStrSensetivityData()
        res = msg1 + "&" + msg2
        print(res)
        tmp.stop_log()

    except Exception:
        tb = sys.exc_info()[2]
        tbinfo = traceback.format_tb(tb)[0]
        pymsg = "PYTHON ERRORS:\nTraceback info:\n" + tbinfo + "\nError Info:\n" + str(sys.exc_info()[1])
        print(pymsg)



# See PyCharm help at https://www.jetbrains.com/help/pycharm/
