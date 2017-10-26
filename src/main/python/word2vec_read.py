
file_name = "glove.6B.300d.txt"

data = {}
with open(file_name) as f:
    for line in f:
        splitted = line.split()
        token = splitted.pop(0)
        data[token] = ([float(x) for x in splitted])
