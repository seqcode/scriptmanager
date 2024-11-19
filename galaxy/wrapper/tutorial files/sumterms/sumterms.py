
import argparse
import pandas as pd

parser = argparse.ArgumentParser()
parser.add_argument('--input',dest= 'tabular_file', required=True, help='Name of the .tabular file which is the output of deseq2 or cuffdiff')
parser.add_argument('--output',dest= 'output_file', required=True, help='Desired name of output file' )

args= parser.parse_args()

data = pd.read_csv(args.tabular_file, sep="\s+", names=['A','B'])
data['C'] = data['A'] + data['B']
print(data)
data.to_csv(args.output_file, mode='a', header=False, index=None, sep='\t')

