import pandas as pd
import argparse

def int_to_Roman(num):
    val = [
        1000, 900, 500, 400,
        100, 90, 50, 40,
        10, 9, 5, 4,
        1
        ]
    syb = [
        "M", "CM", "D", "CD",
        "C", "XC", "L", "XL",
        "X", "IX", "V", "IV",
        "I"
        ]
    roman_num = ''
    i = 0
    while  num > 0:
        for _ in range(num // val[i]):
            roman_num += syb[i]
            num -= val[i]
        i += 1
    return roman_num


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--input', dest='input_arabic_bed', required=True, help='Name of the input bed file with arabic chromosome naming convention')
    parser.add_argument('--output', dest='output_roman_bed', required=True, help='Desired name of the output bed file with Roman chromosome naming convention')
    args = parser.parse_args()
    
    df = pd.read_table(args.input_arabic_bed, names =['chrom', 'start', 'end', 'name', 'score'])
    df['chrom'] = df['chrom'].astype('string').copy()
    df['chrom'] = df['chrom'].str.replace("chr","")
    df['chrom'] = df['chrom'].astype('int').copy()
    df['chrom'] = df['chrom'].apply(int_to_Roman)
    df['chrom'] = 'chr' + df['chrom'].astype('string')
    df.to_csv(args.output_roman_bed, sep= "\t", header = False, index = False)


if __name__ == "__main__":
    main()
