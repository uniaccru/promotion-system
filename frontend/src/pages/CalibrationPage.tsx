import { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Button,
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  MenuItem,
  Chip,
  CircularProgress,
  Alert,
  FormControl,
  InputLabel,
  Select,
  Checkbox,
  Card,
  CardContent,
  Grid,
  Tabs,
  Tab,
  ListItemText,
  IconButton,
} from '@mui/material';
import { Add, PlayArrow, Visibility } from '@mui/icons-material';
import { calibrationService } from '@/services/calibrationService';
import { comparisonService } from '@/services/comparisonService';
import { promotionService } from '@/services/promotionService';
import { employeeService } from '@/services/employeeService';
import { gradeService } from '@/services/gradeService';
import {
  Calibration,
  CreateCalibrationPackageRequest,
  PromotionRequest,
  Grade,
  Employee,
  Comparison,
  CreateComparisonRequest,
  CandidateRanking,
} from '@/types';
import { useAuth } from '@/contexts/AuthContext';
import { format } from 'date-fns';

const CalibrationPage = () => {
  const [calibrations, setCalibrations] = useState<Calibration[]>([]);
  const [grades, setGrades] = useState<Grade[]>([]);
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [promotionRequests, setPromotionRequests] = useState<PromotionRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [openCreateDialog, setOpenCreateDialog] = useState(false);
  const [openComparisonDialog, setOpenComparisonDialog] = useState(false);
  const [openRankingDialog, setOpenRankingDialog] = useState(false);
  const [selectedCalibration, setSelectedCalibration] = useState<Calibration | null>(null);
  const [pendingComparisons, setPendingComparisons] = useState<Comparison[]>([]);
  const [currentComparisonIndex, setCurrentComparisonIndex] = useState(0);
  const [ranking, setRanking] = useState<CandidateRanking | null>(null);
  const [tabValue, setTabValue] = useState(0);
  const { user } = useAuth();
  const userRole = user?.role?.toLowerCase() || '';
  const isHR = userRole === 'hr';
  const isTeamLead = userRole === 'team_lead';

  const [formData, setFormData] = useState<CreateCalibrationPackageRequest>({
    gradeId: 0,
    promotionRequestIds: [],
    evaluatorIds: [],
  });

  useEffect(() => {
    fetchData();
  }, [user]);

  const fetchData = async () => {
    try {
      setLoading(true);
      console.log('Fetching data for user:', user);
      console.log('isHR:', isHR, 'isTeamLead:', isTeamLead, 'employeeId:', user?.employeeId);
      
      if (isHR) {
        console.log('Fetching calibrations for HR...');
        // Load calibrations with all statuses
        const [planning, active, completed] = await Promise.all([
          calibrationService.getCalibrationsByStatus('planning'),
          calibrationService.getCalibrationsByStatus('active'),
          calibrationService.getCalibrationsByStatus('completed')
        ]);
        const allCalibrations = [...(planning || []), ...(active || []), ...(completed || [])];
        console.log('Calibrations data:', allCalibrations);
        setCalibrations(allCalibrations);
        await fetchGrades();
        await fetchEmployees();
        await fetchPromotionRequests();
      } else if (isTeamLead) {
        if (!user?.employeeId) {
          console.error('Team lead user has no employeeId!', user);
          setCalibrations([]);
          return;
        }
        console.log('Fetching calibrations for team lead, employeeId:', user.employeeId);
        const data = await calibrationService.getCalibrationsByEvaluatorId(user.employeeId);
        console.log('Calibrations data for team lead:', data);
        setCalibrations(data || []);
      } else {
        console.log('User is neither HR nor Team Lead. Role:', user?.role);
        setCalibrations([]);
      }
    } catch (error: any) {
      console.error('Failed to fetch data:', error);
      console.error('Error details:', error.response?.data || error.message);
      // Set empty arrays to prevent rendering issues
      setCalibrations([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchGrades = async () => {
    try {
      const data = await gradeService.getAllGrades();
      setGrades(data);
    } catch (error) {
      console.error('Failed to fetch grades:', error);
    }
  };

  const fetchEmployees = async () => {
    try {
      const data = await employeeService.getAllEmployees();
      console.log('All employees:', data);
      // Filter for team leads
      const teamLeads = data.filter(e => e.role.toLowerCase() === 'team_lead');
      console.log('Filtered team leads:', teamLeads);
      setEmployees(teamLeads);
    } catch (error) {
      console.error('Failed to fetch employees:', error);
    }
  };

  const fetchPromotionRequests = async () => {
    try {
      // Try to get all promotion requests first to see what statuses exist
      const allRequests = await promotionService.getAllPromotionRequests();
      console.log('All promotion requests:', allRequests);
      
      // Filter for requests that are ready for calibration or pending/under_review
      // (they might not have status 'ready_for_calibration' yet)
      const readyRequests = allRequests.filter(pr => 
        pr.status === 'ready_for_calibration' || 
        pr.status === 'pending' || 
        pr.status === 'under_review' ||
        (pr.status !== 'in_calibration' && pr.status !== 'approved' && pr.status !== 'rejected')
      );
      console.log('Filtered promotion requests:', readyRequests);
      setPromotionRequests(readyRequests);
    } catch (error) {
      console.error('Failed to fetch promotion requests:', error);
    }
  };

  const handleOpenCreateDialog = () => {
    setFormData({
      gradeId: 0,
      promotionRequestIds: [],
      evaluatorIds: [],
    });
    setOpenCreateDialog(true);
  };

  const handleCloseCreateDialog = () => {
    setOpenCreateDialog(false);
  };

  const handleCreatePackage = async () => {
    try {
      if (formData.promotionRequestIds.length < 3) {
        alert('Необходимо выбрать минимум 3 заявки на повышение');
        return;
      }
      if (formData.evaluatorIds.length !== 2) {
        alert('Необходимо выбрать ровно 2 тимлида');
        return;
      }
      await calibrationService.createCalibrationPackage(formData);
      handleCloseCreateDialog();
      fetchData();
    } catch (error: any) {
      alert('Ошибка при создании пакета: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleStartCalibration = async (calibration: Calibration) => {
    try {
      // If status is planning, change it to active
      if (calibration.status === 'planning') {
        await calibrationService.updateStatus(calibration.id, 'active');
        calibration.status = 'active';
      }
      
      setSelectedCalibration(calibration);
      const pending = await comparisonService.getPendingComparisons(calibration.id);
      console.log('Pending comparisons:', pending);
      
      if (pending.length === 0) {
        alert('Все сравнения уже выполнены. Ожидайте завершения калибровки другим тимлидом.');
        return;
      }
      
      setPendingComparisons(pending);
      setCurrentComparisonIndex(0);
      setOpenComparisonDialog(true);
    } catch (error) {
      console.error('Failed to start calibration:', error);
      alert('Ошибка при запуске калибровки: ' + (error as any).message);
    }
  };

  const handleComparisonSubmit = async (winnerId: number) => {
    if (!selectedCalibration || currentComparisonIndex >= pendingComparisons.length) return;

    const current = pendingComparisons[currentComparisonIndex];
    const request: CreateComparisonRequest = {
      calibrationId: selectedCalibration.id,
      candidateAId: current.candidateAId,
      candidateBId: current.candidateBId,
      winnerId,
    };

    try {
      await comparisonService.createComparison(request);
      
      // Get updated pending comparisons for this evaluator
      const updated = await comparisonService.getPendingComparisons(selectedCalibration.id);
      setPendingComparisons(updated);

      if (updated.length === 0) {
        // This evaluator finished all comparisons
        // Check if both evaluators are done
        const allComparisons = await comparisonService.getComparisonsByCalibrationId(selectedCalibration.id);
        const calibrationDetails = await calibrationService.getCalibrationDetails(selectedCalibration.id);
        
        // Count unique evaluators who made comparisons
        const evaluatorIds = new Set(allComparisons.map(c => c.decidedById));
        const expectedEvaluators = calibrationDetails.evaluatorIds?.length || 0;
        
        // Calculate how many comparisons each evaluator should make
        const candidateCount = calibrationDetails.candidateCount || 0;
        const totalPairs = (candidateCount * (candidateCount - 1)) / 2;
        
        // Count comparisons per evaluator
        const comparisonsPerEvaluator = new Map<number, number>();
        allComparisons.forEach(c => {
          const count = comparisonsPerEvaluator.get(c.decidedById || 0) || 0;
          comparisonsPerEvaluator.set(c.decidedById || 0, count + 1);
        });
        
        console.log('Evaluators who made comparisons:', evaluatorIds.size, 'Expected:', expectedEvaluators);
        console.log('Total pairs needed:', totalPairs);
        console.log('Comparisons per evaluator:', Array.from(comparisonsPerEvaluator.entries()));
        
        // Check if all evaluators completed all their comparisons
        const allEvaluatorsDone = calibrationDetails.evaluatorIds?.every(evalId => {
          const count = comparisonsPerEvaluator.get(evalId) || 0;
          return count >= totalPairs;
        }) || false;
        
        if (allEvaluatorsDone) {
          // Both evaluators finished - calculate ranking and complete calibration
          await calibrationService.updateStatus(selectedCalibration.id, 'completed');
          setOpenComparisonDialog(false);
          fetchData();
          alert('Калибровка завершена! Рейтинг кандидатов рассчитан.');
        } else {
          // This evaluator is done, but waiting for the other one
          setOpenComparisonDialog(false);
          fetchData();
          alert('Вы завершили все сравнения. Ожидайте завершения калибровки другим тимлидом.');
        }
      } else {
        // Move to next comparison
        setCurrentComparisonIndex(0);
      }
    } catch (error) {
      console.error('Failed to submit comparison:', error);
      alert('Ошибка при сохранении сравнения: ' + (error as any).message);
    }
  };

  const handleViewRanking = async (calibration: Calibration) => {
    try {
      // If calibration is active, check if all evaluators finished
      if (calibration.status === 'active') {
        const details = await calibrationService.getCalibrationDetails(calibration.id);
        const comparisons = await comparisonService.getComparisonsByCalibrationId(calibration.id);
        const candidateCount = details.candidateCount || 0;
        const totalPairs = (candidateCount * (candidateCount - 1)) / 2;
        const evaluatorIds = details.evaluatorIds || [];
        
        const comparisonsPerEvaluator = new Map<number, number>();
        comparisons.forEach(comp => {
          const evalId = comp.decidedById || 0;
          comparisonsPerEvaluator.set(evalId, (comparisonsPerEvaluator.get(evalId) || 0) + 1);
        });
        
        const allEvaluatorsDone = evaluatorIds.every(evalId => {
          const count = comparisonsPerEvaluator.get(evalId) || 0;
          return count >= totalPairs;
        });
        
        if (allEvaluatorsDone) {
          // All evaluators finished - update status to completed
          await calibrationService.updateStatus(calibration.id, 'completed');
          calibration.status = 'completed';
          fetchData(); // Refresh the list
        }
      }
      
      const data = await calibrationService.getCandidateRanking(calibration.id);
      setRanking(data);
      setSelectedCalibration(calibration);
      setOpenRankingDialog(true);
    } catch (error) {
      console.error('Failed to fetch ranking:', error);
      alert('Ошибка при получении рейтинга: ' + (error as any).message);
    }
  };

  const handleApprovePromotion = async (promotionRequestId: number) => {
    const comment = prompt('Введите комментарий (необязательно):');
    try {
      await promotionService.approveOrRejectPromotion(promotionRequestId, 'approved', comment || undefined);
      alert('Повышение одобрено! Новый грейд присвоен сотруднику.');
      if (selectedCalibration) {
        handleViewRanking(selectedCalibration);
      }
      fetchData();
    } catch (error: any) {
      console.error('Failed to approve promotion:', error);
      alert(error.response?.data?.message || 'Ошибка при одобрении повышения');
    }
  };

  const handleRejectPromotion = async (promotionRequestId: number) => {
    const comment = prompt('Введите причину отклонения:');
    if (!comment || comment.trim() === '') {
      alert('Необходимо указать причину отклонения');
      return;
    }
    try {
      await promotionService.approveOrRejectPromotion(promotionRequestId, 'rejected', comment);
      alert('Повышение отклонено');
      if (selectedCalibration) {
        handleViewRanking(selectedCalibration);
      }
      fetchData();
    } catch (error: any) {
      console.error('Failed to reject promotion:', error);
      alert(error.response?.data?.message || 'Ошибка при отклонении повышения');
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'planning':
        return 'default';
      case 'active':
        return 'primary';
      case 'completed':
        return 'success';
      default:
        return 'default';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'planning':
        return 'Планирование';
      case 'active':
        return 'Активна';
      case 'completed':
        return 'Завершена';
      default:
        return status;
    }
  };

  if (loading) {
    return (
      <Container maxWidth="lg">
        <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Калибровка</Typography>
        {isHR && (
          <Button variant="contained" startIcon={<Add />} onClick={handleOpenCreateDialog}>
            Создать пакет калибровки
          </Button>
        )}
      </Box>

      {isHR && (
        <Paper sx={{ mb: 3 }}>
          <Tabs value={tabValue} onChange={(_e, v) => setTabValue(v)}>
            <Tab label="Пакеты калибровки" />
            <Tab label="Результаты" />
          </Tabs>
          <Box p={3}>
            {tabValue === 0 && (
              <TableContainer>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>ID</TableCell>
                      <TableCell>Грейд</TableCell>
                      <TableCell>Кандидатов</TableCell>
                      <TableCell>Тимлиды</TableCell>
                      <TableCell>Статус</TableCell>
                      <TableCell>Дата создания</TableCell>
                      <TableCell>Действия</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {calibrations.map((cal) => (
                      <TableRow key={cal.id}>
                        <TableCell>{cal.id}</TableCell>
                        <TableCell>{cal.gradeName || `Grade ${cal.gradeId}`}</TableCell>
                        <TableCell>{cal.candidateCount || 0}</TableCell>
                        <TableCell>
                          {cal.evaluatorNames?.join(', ') || 'Не назначены'}
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={getStatusLabel(cal.status)}
                            color={getStatusColor(cal.status) as any}
                            size="small"
                          />
                        </TableCell>
                        <TableCell>
                          {cal.createdAt ? format(new Date(cal.createdAt), 'dd.MM.yyyy') : '-'}
                        </TableCell>
                        <TableCell>
                          {cal.status === 'completed' && (
                            <IconButton
                              size="small"
                              onClick={() => handleViewRanking(cal)}
                              color="primary"
                            >
                              <Visibility />
                            </IconButton>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
            {tabValue === 1 && (
              <Box>
                <Typography variant="h6" gutterBottom>
                  Результаты калибровок
                </Typography>
                {calibrations
                  .filter(c => c.status === 'completed' || c.status === 'active')
                  .length === 0 ? (
                    <Alert severity="info" sx={{ mt: 2 }}>
                      Нет доступных результатов. Дождитесь завершения калибровок тимлидами.
                    </Alert>
                  ) : (
                    calibrations
                      .filter(c => c.status === 'completed' || c.status === 'active')
                      .map((cal) => (
                        <Card key={cal.id} sx={{ mb: 2 }}>
                          <CardContent>
                            <Box display="flex" justifyContent="space-between" alignItems="center">
                              <Box>
                                <Typography variant="h6">
                                  Пакет #{cal.id} - {cal.gradeName}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                  Кандидатов: {cal.candidateCount}
                                </Typography>
                                <Chip
                                  label={getStatusLabel(cal.status)}
                                  color={getStatusColor(cal.status) as any}
                                  size="small"
                                  sx={{ mt: 1 }}
                                />
                              </Box>
                              <Button
                                variant="outlined"
                                onClick={() => handleViewRanking(cal)}
                              >
                                Просмотреть рейтинг
                              </Button>
                            </Box>
                          </CardContent>
                        </Card>
                      ))
                  )}
              </Box>
            )}
          </Box>
        </Paper>
      )}

      {isTeamLead && (
        <Box>
          <Typography variant="h6" gutterBottom>
            Назначенные калибровки
          </Typography>
          {calibrations.length === 0 ? (
            <Alert severity="info" sx={{ mt: 2 }}>
              У вас нет назначенных калибровок. Ожидайте назначения от HR.
            </Alert>
          ) : (
            <Grid container spacing={2}>
              {calibrations.map((cal) => (
                <Grid item xs={12} md={6} key={cal.id}>
                  <Card>
                    <CardContent>
                      <Typography variant="h6">
                        Пакет #{cal.id} - {cal.gradeName}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        Кандидатов: {cal.candidateCount}
                      </Typography>
                      <Chip
                        label={getStatusLabel(cal.status)}
                        color={getStatusColor(cal.status) as any}
                        size="small"
                        sx={{ mb: 2 }}
                      />
                      <Box>
                        {cal.status === 'planning' && (
                          <Button
                            variant="contained"
                            fullWidth
                            startIcon={<PlayArrow />}
                            onClick={() => handleStartCalibration(cal)}
                          >
                            Начать калибровку
                          </Button>
                        )}
                        {cal.status === 'active' && (
                          <Button
                            variant="contained"
                            fullWidth
                            startIcon={<PlayArrow />}
                            onClick={() => handleStartCalibration(cal)}
                          >
                            Продолжить сравнение
                          </Button>
                        )}
                        {cal.status === 'completed' && (
                          <Alert severity="success">
                            Калибровка завершена
                          </Alert>
                        )}
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      )}

      {/* Create Package Dialog */}
      <Dialog open={openCreateDialog} onClose={handleCloseCreateDialog} maxWidth="md" fullWidth>
        <DialogTitle>Создать пакет калибровки</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <FormControl fullWidth>
              <InputLabel>Грейд</InputLabel>
              <Select
                value={formData.gradeId}
                onChange={(e) => setFormData({ ...formData, gradeId: Number(e.target.value) })}
                label="Грейд"
              >
                {grades.map((grade) => (
                  <MenuItem key={grade.id} value={grade.id}>
                    {grade.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>Заявки на повышение (минимум 3)</InputLabel>
              <Select
                multiple
                value={formData.promotionRequestIds}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    promotionRequestIds: e.target.value as number[],
                  })
                }
                label="Заявки на повышение (минимум 3)"
                renderValue={(selected) =>
                  selected.length === 0
                    ? 'Выберите заявки'
                    : selected
                        .map(
                          (id) =>
                            promotionRequests.find((pr) => pr.id === id)?.employeeName || `#${id}`
                        )
                        .join(', ')
                }
              >
                {promotionRequests.length === 0 ? (
                  <MenuItem disabled>Нет доступных заявок на повышение</MenuItem>
                ) : (
                  promotionRequests
                    .filter((pr) => pr.requestedGradeId === formData.gradeId || formData.gradeId === 0)
                    .map((pr) => (
                      <MenuItem key={pr.id} value={pr.id}>
                        <Checkbox
                          checked={formData.promotionRequestIds.includes(pr.id)}
                        />
                        <ListItemText
                          primary={pr.employeeName}
                          secondary={`Грейд: ${pr.requestedGradeName}, Статус: ${pr.status}`}
                        />
                      </MenuItem>
                    ))
                )}
              </Select>
              {promotionRequests.length === 0 && (
                <Alert severity="info" sx={{ mt: 1 }}>
                  Нет доступных заявок на повышение. Убедитесь, что есть заявки со статусом "pending", "under_review" или "ready_for_calibration".
                </Alert>
              )}
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>Тимлиды (ровно 2)</InputLabel>
              <Select
                multiple
                value={formData.evaluatorIds}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    evaluatorIds: e.target.value as number[],
                  })
                }
                label="Тимлиды (ровно 2)"
                renderValue={(selected) =>
                  selected.length === 0
                    ? 'Выберите тимлидов'
                    : selected
                        .map((id) => employees.find((e) => e.id === id)?.fullName || `#${id}`)
                        .join(', ')
                }
              >
                {employees.length === 0 ? (
                  <MenuItem disabled>Нет доступных тимлидов</MenuItem>
                ) : (
                  employees.map((emp) => (
                    <MenuItem key={emp.id} value={emp.id}>
                      <Checkbox checked={formData.evaluatorIds.includes(emp.id)} />
                      <ListItemText 
                        primary={emp.fullName} 
                        secondary={`${emp.email} (${emp.role})`} 
                      />
                    </MenuItem>
                  ))
                )}
              </Select>
              {employees.length === 0 && (
                <Alert severity="info" sx={{ mt: 1 }}>
                  Нет доступных тимлидов. Убедитесь, что есть сотрудники с ролью "Team Lead" или "team_lead".
                </Alert>
              )}
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseCreateDialog}>Отмена</Button>
          <Button onClick={handleCreatePackage} variant="contained">
            Создать
          </Button>
        </DialogActions>
      </Dialog>

      {/* Comparison Dialog */}
      <Dialog
        open={openComparisonDialog}
        onClose={() => {
          if (window.confirm('Вы уверены, что хотите закрыть? Прогресс будет сохранен, вы сможете продолжить позже.')) {
            setOpenComparisonDialog(false);
            fetchData(); // Refresh to show updated status
          }
        }}
        maxWidth="lg"
        fullWidth
      >
        <DialogTitle>
          Попарное сравнение кандидатов
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Прогресс: {currentComparisonIndex + 1} из {pendingComparisons.length}
          </Typography>
        </DialogTitle>
        <DialogContent>
          {pendingComparisons.length > 0 && currentComparisonIndex < pendingComparisons.length && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="h6" gutterBottom align="center">
                Выберите лучшего кандидата
              </Typography>
              <Grid container spacing={3} sx={{ mt: 2 }}>
                <Grid item xs={6}>
                  <Card
                    sx={{
                      p: 3,
                      cursor: 'pointer',
                      minHeight: '300px',
                      border: '2px solid transparent',
                      '&:hover': { 
                        bgcolor: 'action.hover', 
                        boxShadow: 4,
                        borderColor: 'primary.main'
                      },
                    }}
                    onClick={() =>
                      handleComparisonSubmit(
                        pendingComparisons[currentComparisonIndex].candidateAId
                      )
                    }
                  >
                    <Typography variant="h5" align="center" gutterBottom sx={{ fontWeight: 'bold' }}>
                      {pendingComparisons[currentComparisonIndex].candidateAName}
                    </Typography>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom sx={{ mt: 2 }}>
                      Обоснование:
                    </Typography>
                    <Paper sx={{ p: 2, bgcolor: 'grey.50', maxHeight: '200px', overflow: 'auto' }}>
                      <Typography variant="body2" style={{ whiteSpace: 'pre-wrap' }}>
                        {pendingComparisons[currentComparisonIndex].candidateAJustification || 'Нет обоснования'}
                      </Typography>
                    </Paper>
                  </Card>
                </Grid>
                <Grid item xs={6}>
                  <Card
                    sx={{
                      p: 3,
                      cursor: 'pointer',
                      minHeight: '300px',
                      border: '2px solid transparent',
                      '&:hover': { 
                        bgcolor: 'action.hover', 
                        boxShadow: 4,
                        borderColor: 'primary.main'
                      },
                    }}
                    onClick={() =>
                      handleComparisonSubmit(
                        pendingComparisons[currentComparisonIndex].candidateBId
                      )
                    }
                  >
                    <Typography variant="h5" align="center" gutterBottom sx={{ fontWeight: 'bold' }}>
                      {pendingComparisons[currentComparisonIndex].candidateBName}
                    </Typography>
                    <Typography variant="subtitle2" color="text.secondary" gutterBottom sx={{ mt: 2 }}>
                      Обоснование:
                    </Typography>
                    <Paper sx={{ p: 2, bgcolor: 'grey.50', maxHeight: '200px', overflow: 'auto' }}>
                      <Typography variant="body2" style={{ whiteSpace: 'pre-wrap' }}>
                        {pendingComparisons[currentComparisonIndex].candidateBJustification || 'Нет обоснования'}
                      </Typography>
                    </Paper>
                  </Card>
                </Grid>
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            if (window.confirm('Вы уверены, что хотите закрыть? Прогресс будет сохранен, вы сможете продолжить позже.')) {
              setOpenComparisonDialog(false);
              fetchData();
            }
          }}>
            Закрыть и продолжить позже
          </Button>
        </DialogActions>
      </Dialog>

      {/* Ranking Dialog */}
      <Dialog
        open={openRankingDialog}
        onClose={() => setOpenRankingDialog(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Рейтинг кандидатов</DialogTitle>
        <DialogContent>
          {ranking && (
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Место</TableCell>
                    <TableCell>Кандидат</TableCell>
                    <TableCell>Запрошенный грейд</TableCell>
                    <TableCell>Статус</TableCell>
                    <TableCell>Побед</TableCell>
                    <TableCell>Всего сравнений</TableCell>
                    <TableCell>Процент побед</TableCell>
                    {isHR && <TableCell>Действия</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {ranking.rankings.map((candidate, index) => (
                    <TableRow key={candidate.employeeId}>
                      <TableCell>{index + 1}</TableCell>
                      <TableCell>{candidate.employeeName}</TableCell>
                      <TableCell>{candidate.requestedGradeName}</TableCell>
                      <TableCell>
                        <Chip 
                          label={candidate.currentStatus} 
                          color={
                            candidate.currentStatus === 'approved' ? 'success' :
                            candidate.currentStatus === 'rejected' ? 'error' :
                            candidate.currentStatus === 'calibration_completed' ? 'info' :
                            'default'
                          }
                          size="small"
                        />
                      </TableCell>
                      <TableCell>{candidate.wins}</TableCell>
                      <TableCell>{candidate.totalComparisons}</TableCell>
                      <TableCell>{(candidate.winRate * 100).toFixed(1)}%</TableCell>
                      {isHR && (
                        <TableCell>
                          {candidate.currentStatus === 'calibration_completed' ? (
                            <Box sx={{ display: 'flex', gap: 1 }}>
                              <Button 
                                size="small" 
                                variant="contained" 
                                color="success"
                                onClick={() => handleApprovePromotion(candidate.promotionRequestId)}
                              >
                                Одобрить
                              </Button>
                              <Button 
                                size="small" 
                                variant="outlined" 
                                color="error"
                                onClick={() => handleRejectPromotion(candidate.promotionRequestId)}
                              >
                                Отклонить
                              </Button>
                            </Box>
                          ) : (
                            <Typography variant="body2" color="text.secondary">
                              {candidate.currentStatus === 'approved' ? 'Одобрено' : 
                               candidate.currentStatus === 'rejected' ? 'Отклонено' : '-'}
                            </Typography>
                          )}
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenRankingDialog(false)}>Закрыть</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default CalibrationPage;
