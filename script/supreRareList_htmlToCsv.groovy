@Grab('org.jsoup:jsoup:1.8.3')
@Grab('com.xlson.groovycsv:groovycsv:1.1')
import org.jsoup.Jsoup
import com.xlson.groovycsv.CsvParser

//def str = $/http://wikiwiki.jp/$
//println  new  URL(str).getText("EUC-JP") // 403
def wikiHtml = $/C:\work\20160206\superRare.html/$

// �o�̓t�@�C��
def outputFilePath = $/C:\work\20160206\super_rare_master.csv/$
def outputFile = new File(outputFilePath)
if (outputFile.exists()) {
  outputFile.delete()
}

// �ǂ݂��Ȏ擾�p�ɋ������A�}�C�X�^�[
def oldSupreRareMasterPath = $/C:\work\workspace\guild-story2-search\guild-story2-search\master\super_rare_master.csv/$

// ���A�C�e���}�X�^�[����A�C�e�������擾����B
def getOldSupreRareYomigana = {
  def csv = new File(oldSupreRareMasterPath).text
  def data = new CsvParser().parse(csv).collect{["����": it["����"],"�ǂ݂���":it["�ǂ݂���"]]}
  return {name->
    name ? data.find {it["����"] == name}["�ǂ݂���"] : null
  }
}()

// HTML���璴���A�����擾
def doc = Jsoup.parse(new File(wikiHtml), "EUC-JP")
def supreRareData = []
doc.select("table")[5].select("tr").each {
  def tds = it.select("td")
  if (tds) {
    def superRare = []
    superRare << tds[0].html()
    superRare << tds[1].html().replaceAll('<br class="spacer">', "�@")
    superRare << getOldSupreRareYomigana(tds[0].html())
    supreRareData << superRare
  }
}

// �o��
outputFile.append("ID,����,�ǂ݂���,����", "UTF-8")
outputFile.append("\n", "UTF-8")
supreRareData.eachWithIndex {d, i ->
  outputFile.append([i + 1, d[0], d[2], d[1]].join(","), "UTF-8")
  outputFile.append("\n", "UTF-8")
}

null